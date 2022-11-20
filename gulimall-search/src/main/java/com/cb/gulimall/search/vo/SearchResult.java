package com.cb.gulimall.search.vo;

import com.cb.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * es查询结果
 */
@Data
public class SearchResult {

    /**
     * 商品
     */
    private List<SkuEsModel> products;

    /**
     * 品牌
     */
    private List<BrandVo> brands;

    /**
     * 分类
     */
    private List<CatalogVo> catalogs;

    /**
     * 属性-规则参数
     */
    private List<AttrVo> attrs;

    /**
     * 分页信息
     * 当前页 总页数 商品总数
     */
    private Integer pageNum;
    private Integer totalPages;
    private Long total;
    /**
     * 页码
     */
    private List<Integer> pageNavs;

    /**
     * 面包屑
     */
    private List<NavVo> navs = new ArrayList<>();

    /**
     * 已选中的属性id
     */
    private List<Long> attrIds = new ArrayList<>();

    @Data
    public static class NavVo {
        private String navName;
        private String navValue;
        private String link;
    }

    @Data
    public static class BrandVo {
        // 品牌id
        private Long brandId;
        // 品牌名
        private String brandName;
        // 品牌图片
        private String brandImg;
    }

    @Data
    public static class CatalogVo {
        // 分类id
        private Long catalogId;
        // 分类名
        private String catalogName;
    }

    @Data
    public static class AttrVo {
        // 属性id
        private Long attrId;
        // 属性名
        private String attrName;
        // 属性，多个
        private List<String> attrValue;
    }
}
