package com.cb.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.cb.common.constant.OrderConstant;
import com.cb.common.enume.OrderStatusEnum;
import com.cb.common.exception.NoStockException;
import com.cb.common.utils.R;
import com.cb.common.vo.MemberInfoVo;
import com.cb.gulimall.order.entity.OrderItemEntity;
import com.cb.gulimall.order.feign.CartFeignService;
import com.cb.gulimall.order.feign.MemeberFeignService;
import com.cb.gulimall.order.feign.ProductFeignService;
import com.cb.gulimall.order.feign.WareFeignService;
import com.cb.gulimall.order.interceptor.OrderLoginInterceptor;
import com.cb.gulimall.order.service.OrderItemService;
import com.cb.gulimall.order.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cb.common.utils.PageUtils;
import com.cb.common.utils.Query;

import com.cb.gulimall.order.dao.OrderDao;
import com.cb.gulimall.order.entity.OrderEntity;
import com.cb.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private MemeberFeignService memeberFeignService;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private CartFeignService cartFeignService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 异步线程feign header丢失
     * 异步线程获取不到主线程的header信息，需要手动设置header信息
     */
    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();

        MemberInfoVo memberInfoVo = OrderLoginInterceptor.threadLocal.get();

        System.out.println("主线程：" + Thread.currentThread().getId());

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        // 查询收货地址信息
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            System.out.println("addressFuture：" + Thread.currentThread().getId());
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> addressVos = memeberFeignService.memberArrress(memberInfoVo.getId());
            orderConfirmVo.setAddress(addressVos);
        }, threadPoolExecutor);

        // 查询购物项信息
        CompletableFuture<Void> cartItemFuture = CompletableFuture.runAsync(() -> {
            System.out.println("cartItemFuture：" + Thread.currentThread().getId());
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> orderItemVos = cartFeignService.memberCartItems();
            orderConfirmVo.setItems(orderItemVos);
        }, threadPoolExecutor).thenRunAsync(() -> {
            List<Long> skuIds = orderConfirmVo.getItems().stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            R response = wareFeignService.hashstock(skuIds);
            List<SkuHasStockVo> stockVos = response.getData(new TypeReference<List<SkuHasStockVo>>() {
            });
            Map<Long, Boolean> map = stockVos.stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
            orderConfirmVo.setStocks(map);
        }, threadPoolExecutor);

        // 查询积分信息
        Integer integration = memberInfoVo.getIntegration();
        orderConfirmVo.setIntegration(integration);

        // 幂等性token, 页面和后端各存一份
        String orderToken = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberInfoVo.getId(), orderToken, 30, TimeUnit.MINUTES);
        orderConfirmVo.setOrderToken(orderToken);

        CompletableFuture.allOf(addressFuture, cartItemFuture).get();

        return orderConfirmVo;
    }

    @Transactional(rollbackFor = NoStockException.class)
    @Override
    public SubmitOrderResponseVo orderSubmit(OrderSubmitVo orderSubmitVo) {
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        responseVo.setCode(0);

        MemberInfoVo memberInfoVo = OrderLoginInterceptor.threadLocal.get();

        // 防重token校验，需要保障 token查询、比较、删除院子操作
        String hasTokenScript = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        String tokenKey = OrderConstant.USER_ORDER_TOKEN_PREFIX + memberInfoVo.getId();
        Long result = stringRedisTemplate.execute(new DefaultRedisScript<>(hasTokenScript, Long.class), Arrays.asList(tokenKey), orderSubmitVo.getOrderToken());
        if (result == 0) {
            responseVo.setCode(1);
            return responseVo;
        }

        // 创建订单
        OrderCreateVo orderCreateVo = createOrder(orderSubmitVo, memberInfoVo);

        // 验价,由于精度问题，可能会导致提交支付额与计算的支付额有差异，误差0.01
        if (Math.abs(orderCreateVo.getOrderEntity().getPayAmount().subtract(orderSubmitVo.getPayPrice()).doubleValue()) > 0.01) {
            responseVo.setCode(2);
            return responseVo;
        }

        // 保存订单
        saveOrder(orderCreateVo);

        // 远程锁定库存
        SkuStockLockVo skuStockLockVo = new SkuStockLockVo();
        skuStockLockVo.setOrderSn(orderCreateVo.getOrderEntity().getOrderSn());
        List<OrderItemVo> itemVos = orderCreateVo.getOrderItems().stream().map(it -> {
            OrderItemVo orderItemVo = new OrderItemVo();
            orderItemVo.setSkuId(it.getSkuId()).setTitle(it.getSkuName()).setCount(it.getSkuQuantity());
            return orderItemVo;
        }).collect(Collectors.toList());
        skuStockLockVo.setLocks(itemVos);
        R stockLockResponse = wareFeignService.skuStockLock(skuStockLockVo);
        if (stockLockResponse.getCode() != 0) {
            String msg = (String) stockLockResponse.get("msg");
            throw new NoStockException("库存锁定失败：" + msg);
        }

        responseVo.setOrder(orderCreateVo.getOrderEntity());

        return responseVo;
    }

    private void saveOrder(OrderCreateVo orderCreateVo) {
        OrderEntity orderEntity = orderCreateVo.getOrderEntity();
        Date date = new Date();
        orderEntity.setCreateTime(date).setModifyTime(date);
        this.save(orderEntity);

        Long orderId = orderEntity.getId();
        List<OrderItemEntity> orderItems = orderCreateVo.getOrderItems();
        orderItems.forEach(it -> it.setOrderId(orderId));
        orderItemService.saveBatch(orderItems);
    }

    /**
     * 创建订单
     */
    private OrderCreateVo createOrder(OrderSubmitVo orderSubmitVo, MemberInfoVo memberInfoVo) {
        OrderCreateVo orderCreateVo = new OrderCreateVo();

        OrderEntity orderEntity = buildOrder(orderSubmitVo, memberInfoVo);
        orderCreateVo.setOrderEntity(orderEntity);

        List<OrderItemEntity> orderItems = buildOrderItems(orderEntity.getOrderSn());
        orderCreateVo.setOrderItems(orderItems);

        // 各种金额计算
        computerPrice(orderEntity, orderItems);

        return orderCreateVo;
    }

    private void computerPrice(OrderEntity orderEntity, List<OrderItemEntity> orderItems) {
        // 订单总额
        BigDecimal totalAmount = new BigDecimal("0.0");
        // 促销优化金额（促销价、满减、阶梯价）
        BigDecimal promotionAmount = new BigDecimal("0.0");
        // 积分抵扣金额
        BigDecimal integrationAmount = new BigDecimal("0.0");
        // 优惠券抵扣金额
        BigDecimal couponAmount = new BigDecimal("0.0");
        // 积分
        Integer integration = 0;
        // 成长值
        Integer growth = 0;
        for (OrderItemEntity orderItem : orderItems) {
            totalAmount = totalAmount.add(orderItem.getRealAmount());
            promotionAmount = promotionAmount.add(orderItem.getPromotionAmount());
            integrationAmount = integrationAmount.add(orderItem.getIntegrationAmount());
            couponAmount = couponAmount.add(orderItem.getCouponAmount());
            integration = integration + orderItem.getGiftIntegration();
            growth = growth + orderItem.getGiftGrowth();
        }
        BigDecimal payAmount = totalAmount.add(orderEntity.getFreightAmount())
                .subtract(promotionAmount).subtract(integrationAmount).subtract(couponAmount);
        orderEntity.setPromotionAmount(promotionAmount)
                .setIntegrationAmount(integrationAmount)
                .setCouponAmount(couponAmount)
                .setIntegration(integration)
                .setGrowth(growth)
                .setPayAmount(payAmount)
                .setTotalAmount(totalAmount);
    }

    private List<OrderItemEntity> buildOrderItems(String orderSn) {

        // 这里是最后一次来确认购物项的价格 这个远程方法还会查询一次数据库
        List<OrderItemVo> orderItemVos = cartFeignService.memberCartItems();
        List<OrderItemEntity> itemEntities = null;
        if (orderItemVos != null && orderItemVos.size() > 0) {
            itemEntities = orderItemVos.stream().map(it -> {
                OrderItemEntity itemEntity = buildOrderItem(it);
                itemEntity.setOrderSn(orderSn);
                return itemEntity;
            }).collect(Collectors.toList());
        }
        return itemEntities;
    }

    private OrderItemEntity buildOrderItem(OrderItemVo orderItemVo) {
        OrderItemEntity itemEntity = new OrderItemEntity();

        // spu信息
        R spuInfoResponse = productFeignService.selectSpuInfo(orderItemVo.getSkuId());
        SpuInfoVo spuInfoVo = spuInfoResponse.getData(new TypeReference<SpuInfoVo>() {
        });
        itemEntity.setSpuId(spuInfoVo.getId())
                .setSpuName(spuInfoVo.getSpuName())
                .setSpuBrand(spuInfoVo.getBrandId().toString())
                // 分类
                .setCategoryId(spuInfoVo.getCatalogId());

        // sku信息
        itemEntity.setSkuId(orderItemVo.getSkuId())
                .setSkuName(orderItemVo.getTitle())
                .setSkuPic(orderItemVo.getImage())
                .setSkuPrice(orderItemVo.getPrice())
                .setSkuQuantity(orderItemVo.getCount())
                .setSkuAttrsVals(StringUtils.collectionToDelimitedString(orderItemVo.getSkuAttrValues(), ";"));

        // 成长值
        itemEntity.setGiftGrowth(orderItemVo.getPrice().multiply(new BigDecimal(orderItemVo.getCount())).intValue())
                .setGiftIntegration(orderItemVo.getPrice().multiply(new BigDecimal(orderItemVo.getCount())).intValue());

        // 优惠信息
        itemEntity.setPromotionAmount(new BigDecimal("0.0"))
                .setCouponAmount(new BigDecimal("0.0"))
                .setIntegrationAmount(new BigDecimal("0.0"));

        // 优惠后金额
        BigDecimal realAmount = orderItemVo.getPrice().multiply(new BigDecimal(orderItemVo.getCount().toString()))
                .subtract(itemEntity.getPromotionAmount())
                .subtract(itemEntity.getCouponAmount())
                .subtract(itemEntity.getIntegrationAmount());
        itemEntity.setRealAmount(realAmount);

        return itemEntity;
    }

    private OrderEntity buildOrder(OrderSubmitVo orderSubmitVo, MemberInfoVo memberInfoVo) {
        OrderEntity orderEntity = new OrderEntity();

        // 订单号
        orderEntity.setOrderSn(IdWorker.getTimeId())
                // 用户信息
                .setMemberId(memberInfoVo.getId()).setMemberUsername(memberInfoVo.getUsername())
                // 备注
                .setNote(orderSubmitVo.getNote())
                // 自动确认时间
                .setAutoConfirmDay(7)
                // 初始状态，待付款
                .setStatus(OrderStatusEnum.CREATE_NEW.getCode())
                // 订单来源
                .setSourceType(orderSubmitVo.getSourceType())
                // 发票类型
                .setBillType(0);

        R fareResponse = wareFeignService.fare(orderSubmitVo.getAddrId());
        FareVo fareVo = fareResponse.getData(new TypeReference<FareVo>() {
        });
        MemberAddressVo memberAddressVo = fareVo.getMemberAddressVo();
        // 运费
        orderEntity.setFreightAmount(fareVo.getFare())
                // 收货人及收货地址信息
                .setReceiverName(memberAddressVo.getName())
                .setReceiverPhone(memberAddressVo.getPhone())
                .setReceiverPostCode(memberAddressVo.getPostCode())
                .setReceiverProvince(memberAddressVo.getProvince())
                .setReceiverCity(memberAddressVo.getCity())
                .setReceiverRegion(memberAddressVo.getRegion())
                .setReceiverDetailAddress(memberAddressVo.getDetailAddress());

        return orderEntity;
    }

}