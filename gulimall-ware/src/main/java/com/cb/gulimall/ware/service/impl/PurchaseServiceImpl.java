package com.cb.gulimall.ware.service.impl;

import com.cb.common.constant.WareConstant;
import com.cb.common.exception.RRException;
import com.cb.gulimall.ware.entity.PurchaseDetailEntity;
import com.cb.gulimall.ware.service.PurchaseDetailService;
import com.cb.gulimall.ware.service.WareSkuService;
import com.cb.gulimall.ware.vo.MergeVo;
import com.cb.gulimall.ware.vo.PurchaseDetailDoneVo;
import com.cb.gulimall.ware.vo.PurchaseDoneVo;
import com.cb.gulimall.ware.vo.StockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cb.common.utils.PageUtils;
import com.cb.common.utils.Query;

import com.cb.gulimall.ware.dao.PurchaseDao;
import com.cb.gulimall.ware.entity.PurchaseEntity;
import com.cb.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Resource
    private PurchaseDetailService purchaseDetailService;

    @Autowired
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceive(Map<String, Object> params) {
        QueryWrapper<PurchaseEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(PurchaseEntity::getStatus, WareConstant.PurchaseOrderEnum.CREATED.getCode());
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void merge(MergeVo mergeVo) {
        // 1、有采购单 2、没有采购单，需要新建，状态未分配
        Long purchaseId = mergeVo.getPurchaseId();
        Date date = new Date();
        if (purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setCreateTime(date)
                    .setUpdateTime(date)
                    .setStatus(WareConstant.PurchaseOrderEnum.CREATED.getCode());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        } else {
            // 采购单状态需要是新建/已分配
            PurchaseEntity purchaseEntity = this.baseMapper.selectById(purchaseId);
            if (!purchaseEntity.getStatus().equals(WareConstant.PurchaseOrderEnum.CREATED.getCode()) &&
                    !purchaseEntity.getStatus().equals(WareConstant.PurchaseOrderEnum.ASSIGNEE.getCode())) {
                throw new RRException("采购单状态需要是新建/已分配: " + purchaseId);
            }
        }

        // 批量更新采购需求状态-已分配
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailService.listByIds(mergeVo.getItems());
        List<PurchaseDetailEntity> errList = purchaseDetailEntities.stream().filter(it ->
                !it.getStatus().equals(WareConstant.PurchaseNeedsEnum.CREATED.getCode()) &&
                        !it.getStatus().equals(WareConstant.PurchaseNeedsEnum.ASSIGNEE.getCode())
        ).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(errList)) {
            List<Long> ids = errList.stream().map(PurchaseDetailEntity::getId).collect(Collectors.toList());
            throw new RRException("采购需求状态需要是新建/已分配: " + ids);
        }
        List<PurchaseDetailEntity> detailEntityList = purchaseDetailEntities.stream().map(it -> {
            PurchaseDetailEntity db = purchaseDetailService.getById(it);
            if (!db.getStatus().equals(WareConstant.PurchaseNeedsEnum.CREATED.getCode()) &&
                    !db.getStatus().equals(WareConstant.PurchaseNeedsEnum.ASSIGNEE.getCode())) {

            }
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            detailEntity.setPurchaseId(finalPurchaseId)
                    .setId(it.getId())
                    .setStatus(WareConstant.PurchaseNeedsEnum.ASSIGNEE.getCode());
            return detailEntity;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(detailEntityList);
    }

    @Override
    @Transactional
    public void recived(List<Long> ids) {
        // 采购单状态需要是已分配
        List<PurchaseEntity> purchaseList = this.baseMapper.selectBatchIds(ids);
        List<PurchaseEntity> collect = purchaseList.stream().filter(it ->
                !it.getStatus().equals(WareConstant.PurchaseNeedsEnum.ASSIGNEE.getCode())
        ).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(collect)) {
            List<Long> errIds = collect.stream().map(PurchaseEntity::getId).collect(Collectors.toList());
            throw new RRException("采购单状态需要是已分配: " + errIds);
        }

        // 采购单状变更为已分配->已领取
        List<PurchaseEntity> purchaseEntities = purchaseList.stream().map(it -> {
            PurchaseEntity entity = new PurchaseEntity();
            entity.setUpdateTime(new Date()).setStatus(WareConstant.PurchaseOrderEnum.RECIVED.getCode()).setId(it.getId());
            return entity;
        }).collect(Collectors.toList());
        this.updateBatchById(purchaseEntities);

        // 采购需求需要是已分配,状态变更为已分配->正在采购
        for (PurchaseEntity it : purchaseList) {
            List<PurchaseDetailEntity> purchaseDetailList = purchaseDetailService.listByPurchaseId(it.getId());
            if (CollectionUtils.isEmpty(purchaseDetailList)) {
                throw new RRException("采购单下没有采购需求: " + it.getId());
            }
            List<PurchaseDetailEntity> pdList = purchaseDetailList.stream().map(entity -> {
                PurchaseDetailEntity pd = new PurchaseDetailEntity();
                pd.setId(entity.getId()).setStatus(WareConstant.PurchaseNeedsEnum.BUYING.getCode());
                return pd;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(pdList);
        }
    }

    @Override
    @Transactional
    public void done(PurchaseDoneVo purchaseDoneVo) {
        // 1、更新采购单状态
        Long id = purchaseDoneVo.getId();
        Boolean flag = true;

        // 2、更新采购项状态
        List<PurchaseDetailEntity> pds = new ArrayList<>();
        List<PurchaseDetailDoneVo> items = purchaseDoneVo.getItems();
        for (PurchaseDetailDoneVo item : items) {
            PurchaseDetailEntity pd = new PurchaseDetailEntity();
            pd.setId(item.getItemId());
            if (item.getStatus().equals(WareConstant.PurchaseNeedsEnum.FAIL.getCode())) {
                pd.setStatus(item.getStatus());
                flag = false;
            } else {
                pd.setStatus(WareConstant.PurchaseNeedsEnum.FINISHED.getCode());
                // 3采购成功的录入库存
                PurchaseDetailEntity detailEntity = purchaseDetailService.getById(item.getItemId());
                StockVo stockVo = new StockVo()
                        .setSkuId(detailEntity.getSkuId())
                        .setStock(detailEntity.getSkuNum())
                        .setWareId(detailEntity.getWareId());
                wareSkuService.addStock(stockVo);
            }
            pds.add(pd);
        }
        purchaseDetailService.updateBatchById(pds);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag ? WareConstant.PurchaseOrderEnum.FINISHED.getCode() : WareConstant.PurchaseOrderEnum.HASERROR.getCode());
        this.updateById(purchaseEntity);


    }

}