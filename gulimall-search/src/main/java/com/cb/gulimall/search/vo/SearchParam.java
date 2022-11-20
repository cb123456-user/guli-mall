package com.cb.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 全文检索可能的参数
 * keyword=华为&
 * catalog3Id=225&
 * brandId=8&brandId=9&
 * sort=hotScore_desc&
 * hasStock=1&
 * skuPrice=1_5000&
 * attrs=1_0-安卓（Android）:苹果（IOS）&attrs=2_5.56英寸及以上:5.5-5.1英寸
 */
@Data
public class SearchParam {

    /**
     * 关键字全文检索
     */
    private String keyword;

    /**
     * 三级分类id
     */
    private Long catalog3Id;

    /**
     * 品牌id，支持多选
     */
    private List<Long> brandId;

    /**
     * 价格区间/销量/热度排序
     * hotScore_desc asc
     * saleCount_desc asc
     * skuPrice_desc asc
     */
    private String sort;

    /**
     * 是否有库存，默认有库存1
     * hasStock=0/1
     */
    private Integer hasStock;

    /**
     * 价格区间
     * skuPrice=1_5000
     * skuPrice=_5000
     * skuPrice=5000_
     */
    private String skuPrice;

    /**
     * 规格参数,支持多个
     * attrs=2_5.56英寸及以上:5.5-5.1英寸
     */
    private List<String> attrs;

    /**
     * 页码，默认第一页
     */
    private Integer pageNum = 1;

    /**
     * 原生请求参数
     */
    private String queryString;
}
