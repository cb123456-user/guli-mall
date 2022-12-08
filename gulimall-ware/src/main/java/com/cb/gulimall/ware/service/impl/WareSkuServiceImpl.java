package com.cb.gulimall.ware.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.TypeReference;
import com.cb.common.constant.WareConstant;
import com.cb.common.enume.OrderStatusEnum;
import com.cb.common.exception.NoStockException;
import com.cb.common.exception.RRException;
import com.cb.common.to.OrderTo;
import com.cb.common.to.StockLockedTo;
import com.cb.common.utils.R;
import com.cb.gulimall.ware.entity.PurchaseDetailEntity;
import com.cb.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.cb.gulimall.ware.entity.WareOrderTaskEntity;
import com.cb.gulimall.ware.feign.OrderFeignService;
import com.cb.gulimall.ware.feign.ProductFeignService;
import com.cb.gulimall.ware.service.WareOrderTaskDetailService;
import com.cb.gulimall.ware.service.WareOrderTaskService;
import com.cb.gulimall.ware.vo.OrderItemVo;
import com.cb.gulimall.ware.vo.SkuHasStockVo;
import com.cb.gulimall.ware.vo.SkuStockLockVo;
import com.cb.gulimall.ware.vo.StockVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cb.common.utils.PageUtils;
import com.cb.common.utils.Query;

import com.cb.gulimall.ware.dao.WareSkuDao;
import com.cb.gulimall.ware.entity.WareSkuEntity;
import com.cb.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


@Service("wareSkuService")
@Slf4j
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private WareOrderTaskService wareOrderTaskService;

    @Autowired
    private WareOrderTaskDetailService wareOrderTaskDetailService;

    @Autowired
    private OrderFeignService orderFeignService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /**
         * wareId: 123,//仓库id
         *    skuId: 123//商品id
         */
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.lambda().eq(WareSkuEntity::getSkuId, skuId);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.lambda().like(WareSkuEntity::getWareId, wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void addStock(StockVo stockVo) {
        // skuId无库存，新增; 有则更新,库存累加
        List<WareSkuEntity> entityList = list(new QueryWrapper<WareSkuEntity>().lambda()
                .eq(WareSkuEntity::getSkuId, stockVo.getSkuId())
                .eq(WareSkuEntity::getWareId, stockVo.getWareId())
        );
        if (!CollectionUtils.isEmpty(entityList)) {
            this.baseMapper.updateStock(stockVo);
            return;
        }

        WareSkuEntity wareSkuEntity = new WareSkuEntity();
        try {
            // 新增库存时冗余存储sku_name
            R info = productFeignService.info(stockVo.getSkuId());
            if (info.getCode() == 0) {
                String skuName = (String) BeanUtil.beanToMap(info.get("skuInfo")).get("skuName");
                wareSkuEntity.setSkuName(skuName);
            }
        } catch (Exception e) {
            log.warn("ware feign product fail, path： /product/skuinfo/info/{skuId}");
        }
        wareSkuEntity.setSkuId(stockVo.getSkuId()).setStock(stockVo.getStock()).setWareId(stockVo.getWareId())
                .setStockLocked(0);
        save(wareSkuEntity);
    }

    @Override
    public List<SkuHasStockVo> hasStock(List<Long> skuIdList) {
        List<SkuHasStockVo> stockVos = skuIdList.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();
            // 注意，库存可能为空，需要用包装类接收数据
            Long stock = baseMapper.getSkuStockskuId(skuId);
            vo.setSkuId(skuId).setHasStock(stock == null ? false : stock > 0);
            return vo;
        }).collect(Collectors.toList());
        return stockVos;
    }

    @Transactional
    @Override
    public void skuStockLock(SkuStockLockVo skuStockLockVo) {

        // 存储库存锁定清单
        WareOrderTaskEntity orderTaskEntity = new WareOrderTaskEntity().setOrderSn(skuStockLockVo.getOrderSn());
        wareOrderTaskService.save(orderTaskEntity);

        // [理论上]1. 按照下单的收获地址 找到一个就近仓库, 锁定库存
        // [实际上]1. 找到每一个商品在那个一个仓库有库存
        for (OrderItemVo orderItemVo : skuStockLockVo.getLocks()) {
            // 查询当前商品有库存的仓库
            List<Long> wareIds = this.baseMapper.selectSkuHasStockWare(orderItemVo.getSkuId());
            // 没有仓库有库存，不用继续
            if (CollectionUtils.isEmpty(wareIds)) {
                throw new NoStockException(orderItemVo.getSkuId());
            }
            // 依次锁定库存
            boolean skuLock = false;
            for (Long wareId : wareIds) {
                // 锁库存
                Long stockLock = this.baseMapper.skuStockLock(orderItemVo.getSkuId(), wareId, orderItemVo.getCount());
                // 当前仓库库存锁定失败，下一个
                if(stockLock > 0){
                    skuLock = true;
                    // 库存锁定清单明细
                    WareOrderTaskDetailEntity taskDetailEntity = new WareOrderTaskDetailEntity()
                            .setTaskId(orderTaskEntity.getId())
                            .setSkuId(orderItemVo.getSkuId())
                            .setWareId(wareId).setSkuNum(orderItemVo.getCount())
                            .setLockStatus(WareConstant.WareOrderTaskStatusEnum.LOCKED.getStatus());
                    wareOrderTaskDetailService.save(taskDetailEntity);
                    // 解库存的消息
                    StockLockedTo stockLockedTo = new StockLockedTo();
                    BeanUtil.copyProperties(taskDetailEntity, stockLockedTo);
                    stockLockedTo.setOrderSn(skuStockLockVo.getOrderSn());
                    rabbitTemplate.convertAndSend(
                            "stock-event-exchange",
                            "stock.locked",
                            stockLockedTo,
                            new CorrelationData(UUID.randomUUID().toString())
                    );
                    break;
                }
            }
            // 全部仓库都库存锁定失败
            if(!skuLock){
                throw new NoStockException(orderItemVo.getSkuId());
            }
        }
    }

    /**
     * 根据库存锁定清单解锁库存
     *  1、库存锁定清单不存在，说明是库存锁定时出问题，数据回滚了，不用解锁库存
     *  2、库存锁定清单存在，说明库存锁定成功了，但是是否回滚需要看订单状态
     *      没有这个订单，业务回滚，必须解锁库存
     *      存在 4-已取消，说明是用户自己取消 / 超时未支付，这种情况需要解锁库存
     * @param stockLockedTo
     */
    @Override
    @Transactional
    public void stockRelease(StockLockedTo stockLockedTo) {
        WareOrderTaskDetailEntity orderTaskDetailEntity = wareOrderTaskDetailService.getById(stockLockedTo.getId());
        if (orderTaskDetailEntity == null) {
            return;
        }
        // 库存锁定的才需要解锁
        if (!WareConstant.WareOrderTaskStatusEnum.LOCKED.getStatus().equals(orderTaskDetailEntity.getLockStatus())) {
            return;
        }
        // 远程查询订单状态
        R r = orderFeignService.selectOrderStatus(stockLockedTo.getOrderSn());
        if (r.getCode() != 0) {
            throw new RRException("远程调用订单服务失败，orderSn= " + stockLockedTo.getOrderSn());
        }
        OrderTo orderTo = r.getData(new TypeReference<OrderTo>() {
        });
        if (orderTo == null || OrderStatusEnum.CANCLED.getCode().equals(orderTo.getStatus())) {
            unLockStock(orderTaskDetailEntity.getSkuId(), orderTaskDetailEntity.getWareId(), orderTaskDetailEntity.getSkuNum(), orderTaskDetailEntity.getId());
        }
    }

    /**
     * 订单关闭后补偿消息，防止解锁库存消息先执行后导致库存无法解锁
     * @param orderTo
     */
    @Override
    @Transactional
    public void stockRelease(OrderTo orderTo) {
        WareOrderTaskEntity wareOrderTaskEntity = wareOrderTaskService.getOrderTaskByOrderSn(orderTo.getOrderSn());
        Long taskId = wareOrderTaskEntity.getId();
        // 保证是已锁定的才需要解锁，业务保证幂等性
        List<WareOrderTaskDetailEntity> detailEntities = wareOrderTaskDetailService.getOrderTaskDetailByTaskId(taskId);
        for (WareOrderTaskDetailEntity taskDetailEntity : detailEntities) {
            unLockStock(taskDetailEntity.getSkuId(), taskDetailEntity.getWareId(), taskDetailEntity.getSkuNum(), taskDetailEntity.getId());
        }
    }

    public void unLockStock(Long skuId, Long wareId, Integer num, Long taskDetailId){
        // 库存解锁
        this.baseMapper.unLockStock(skuId, wareId, num);

        // 工资单明细状态变更
        WareOrderTaskDetailEntity taskDetail = new WareOrderTaskDetailEntity()
                .setId(taskDetailId)
                .setLockStatus(WareConstant.WareOrderTaskStatusEnum.UNLOCK.getStatus());
        wareOrderTaskDetailService.updateById(taskDetail);
    }

}