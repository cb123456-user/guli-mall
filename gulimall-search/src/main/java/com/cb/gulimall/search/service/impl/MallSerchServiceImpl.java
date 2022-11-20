package com.cb.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.cb.common.to.es.SkuEsModel;
import com.cb.common.utils.R;
import com.cb.gulimall.search.config.ElasticSearchConfig;
import com.cb.gulimall.search.constant.ElasticConstant;
import com.cb.gulimall.search.feign.ProductFeiginService;
import com.cb.gulimall.search.service.MallSerchService;
import com.cb.gulimall.search.vo.AttrRespVo;
import com.cb.gulimall.search.vo.BrandVo;
import com.cb.gulimall.search.vo.SearchParam;
import com.cb.gulimall.search.vo.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.TotalHits;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MallSerchServiceImpl implements MallSerchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private ProductFeiginService productFeiginService;

    @Override
    public SearchResult search(SearchParam searchParam) {
        SearchResult searchResult = null;

        // 1、构建查询参数
        SearchRequest searchRequest = buildRequestParam(searchParam);

        try {
            // 2.es查询
            SearchResponse response = restHighLevelClient.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);

            // 3.封装查询结果
            searchResult = buildResult(response, searchParam);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return searchResult;
    }

    /**
     * 构建查询参数
     *
     * @param searchParam
     * @return
     */
    private SearchRequest buildRequestParam(SearchParam searchParam) {

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 1.检索
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 1.1 Keyword
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            boolQuery.must(QueryBuilders.termQuery("skuTitle", searchParam.getKeyword()));
        }
        // 1.2 catalogId
        if (searchParam.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", searchParam.getCatalog3Id()));
        }
        // 1.3 brandId
        if (!CollectionUtils.isEmpty(searchParam.getBrandId())) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", searchParam.getBrandId()));
        }
        // 1.4 hasStock 0/1
        if (searchParam.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", searchParam.getHasStock() == 1));
        }
        // 1.5 skuPrice
        if (!StringUtils.isEmpty(searchParam.getSkuPrice())) {
            String[] skuPrices = searchParam.getSkuPrice().split("_");
            // 1_5000
            if (skuPrices.length == 2) {
                boolQuery.filter(QueryBuilders.rangeQuery("skuPrice").from(skuPrices[0]).to(skuPrices[1]));
            }
            // 1_ 或 _5000
            if (skuPrices.length == 1) {
                // 1_
                if (searchParam.getSkuPrice().startsWith("_")) {
                    boolQuery.filter(QueryBuilders.rangeQuery("skuPrice").from(skuPrices[0]));
                }
                // _5000
                if (searchParam.getSkuPrice().endsWith("_")) {
                    boolQuery.filter(QueryBuilders.rangeQuery("skuPrice").to(skuPrices[0]));
                }
            }
        }
        // 1.6 attrs,一条属性一条nested查询
        if (!CollectionUtils.isEmpty(searchParam.getAttrs())) {
            for (String attr : searchParam.getAttrs()) {
                // attrs=2_5.56英寸及以上:5.5-5.1英寸
                String[] attrArr = attr.split("_");
                String attrId = attrArr[0];
                String[] attrValues = attrArr[1].split(":");
                BoolQueryBuilder builder = QueryBuilders.boolQuery();
                builder.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                builder.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                boolQuery.filter(QueryBuilders.nestedQuery("attrs", builder, ScoreMode.None));
            }
        }
        sourceBuilder.query(boolQuery);
        // 2.高亮
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            HighlightBuilder highlight = new HighlightBuilder();
            highlight.field("skuTitle");
            highlight.preTags("<b style='color:red'>");
            highlight.postTags("</b>");
            sourceBuilder.highlighter(highlight);
        }
        // 3.聚合
        // 品牌聚合brand_agg
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg").field("brandId").size(50);
        // 子聚合brand_name_agg
        TermsAggregationBuilder brand_name_agg = AggregationBuilders.terms("brand_name_agg").field("brandName").size(1);
        brand_agg.subAggregation(brand_name_agg);
        // 子聚合brand_img_agg
        TermsAggregationBuilder brand_img_agg = AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1);
        brand_agg.subAggregation(brand_img_agg);
        sourceBuilder.aggregation(brand_agg);
        // 分类聚合catalog_agg
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(50);
        // 子聚合catalog_name_agg
        TermsAggregationBuilder catalog_name_agg = AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1);
        catalog_agg.subAggregation(catalog_name_agg);
        sourceBuilder.aggregation(catalog_agg);
        // 属性聚合attr_agg
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        // attr_id_agg
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(50);
        // attr_name_agg
        TermsAggregationBuilder attr_name_agg = AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1);
        attr_id_agg.subAggregation(attr_name_agg);
        // attr_value_agg
        TermsAggregationBuilder attr_value_agg = AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50);
        attr_id_agg.subAggregation(attr_value_agg);
        attr_agg.subAggregation(attr_id_agg);
        sourceBuilder.aggregation(attr_agg);

        // 4.排序 skuPrice_desc asc
        if (!StringUtils.isEmpty(searchParam.getSort())) {
            String[] sortArr = searchParam.getSort().split("_");
            SortOrder sortOrder = "desc".equalsIgnoreCase(sortArr[1]) ? SortOrder.DESC : SortOrder.ASC;
            sourceBuilder.sort(sortArr[0], sortOrder);
        }

        // 5.分页
        // (当前页 - 1) * 每页数量
        sourceBuilder.from((searchParam.getPageNum() - 1) * ElasticConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(ElasticConstant.PRODUCT_PAGESIZE);

        System.out.println(sourceBuilder.toString());

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(ElasticConstant.PRODUCT_INDEX);
        searchRequest.source(sourceBuilder);

        return searchRequest;
    }

    /**
     * 封装查询结果
     *
     * @param response
     * @return
     */
    private SearchResult buildResult(SearchResponse response, SearchParam param) {
        SearchResult result = new SearchResult();

        SearchHits hits = response.getHits();
        Aggregations aggregations = response.getAggregations();

        // 商品信息
        if (hits.getHits() != null && hits.getHits().length > 0) {
            List<SkuEsModel> skuEsModelList = new ArrayList<>();
            for (SearchHit hit : hits.getHits()) {
                SkuEsModel skuEsModel = JSON.parseObject(hit.getSourceAsString(), SkuEsModel.class);
                // 高亮,keyword不为空，就会有高亮
                if (!StringUtils.isEmpty(param.getKeyword())) {
                    skuEsModel.setSkuTitle(hit.getHighlightFields().get("skuTitle").getFragments()[0].toString());
                }
                skuEsModelList.add(skuEsModel);
            }
            result.setProducts(skuEsModelList);
        }

        // 分类信息
        List<SearchResult.CatalogVo> catalogs = new ArrayList<>();
        ParsedLongTerms catalog_agg = aggregations.get("catalog_agg");
        for (Terms.Bucket bucket : catalog_agg.getBuckets()) {
            Long catalogId = Long.valueOf(bucket.getKeyAsString());
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String catalogName = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            catalogVo.setCatalogId(catalogId);
            catalogVo.setCatalogName(catalogName);
            catalogs.add(catalogVo);
        }
        result.setCatalogs(catalogs);

        // 品牌信息brand_agg
        List<SearchResult.BrandVo> brands = new ArrayList<>();
        ParsedLongTerms brand_agg = aggregations.get("brand_agg");
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            Long brandId = Long.valueOf(bucket.getKeyAsString());
            String brandImg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
            String brandName = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            brandVo.setBrandId(brandId);
            brandVo.setBrandImg(brandImg);
            brandVo.setBrandName(brandName);
            brands.add(brandVo);
        }
        result.setBrands(brands);

        // 属性
        List<SearchResult.AttrVo> attrs = new ArrayList<>();
        ParsedNested attr_agg = aggregations.get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            Long attrId = Long.valueOf(bucket.getKeyAsString());
            String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            List<String> attrValue = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream()
                    .map(it -> it.getKeyAsString()).collect(Collectors.toList());
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValue(attrValue);
            attrs.add(attrVo);
        }
        result.setAttrs(attrs);

        // 当前页
        result.setPageNum(param.getPageNum());
        // 总记录数
        TotalHits totalHits = hits.getTotalHits();
        result.setTotal(totalHits.value);
        // 总页数
        Integer totalPage = (int) totalHits.value % ElasticConstant.PRODUCT_PAGESIZE == 0 ? (int) totalHits.value / ElasticConstant.PRODUCT_PAGESIZE : (int) totalHits.value / ElasticConstant.PRODUCT_PAGESIZE + 1;
        result.setTotalPages(totalPage);

        // 页码
        List<Integer> pageNavs = new ArrayList<>();
        for (Integer i = 0; i < totalPage; i++) {
            pageNavs.add(i + 1);
        }
        result.setPageNavs(pageNavs);

        // 面包屑-属性 attrs=2_5.56英寸及以上:5.5-5.1英寸
        if (!CollectionUtils.isEmpty(param.getAttrs())) {
            String queryString = param.getQueryString();
            List<Long> arrIds = new ArrayList<>();
            List<SearchResult.NavVo> navVos = param.getAttrs().stream().map(it -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] arr = it.split("_");
                // 属性名
                R r = productFeiginService.attrInfo(Long.parseLong(arr[0]));
                if (r.getCode() == 0) {
                    AttrRespVo attrRespVo = r.getData("attr", new TypeReference<AttrRespVo>() {
                    });
                    navVo.setNavName(attrRespVo.getAttrName());
                } else {
                    navVo.setNavName(arr[0]);
                }
                // 属性值
                navVo.setNavValue(arr[1].replace(":", "、"));
                // 链接,去除当前属性参数
                String value = arr[1];
                // 中文需要先编码，空格前端编码为%20,java转码为+，需要再次替换
                try {
                    value = URLEncoder.encode(value, "UTF-8").replace("+", "%20");
                } catch (UnsupportedEncodingException e) {

                }
                // ?attrs=  ?keywork=&attrs= ?
                String link = queryString.replace("&attrs=" + arr[0] + "_" + value, "")
                        .replace("attrs=" + arr[0] + "_" + value, "");
                if (link.startsWith("&")) {
                    link = link.substring(1);
                }
                if (StringUtils.isEmpty(link)) {
                    navVo.setLink(ElasticConstant.SEARCH_URL_1 + link);
                } else {
                    navVo.setLink(ElasticConstant.SEARCH_URL_2 + link);
                }

                // 记录已选属性
                arrIds.add(Long.parseLong(arr[0]));

                return navVo;
            }).collect(Collectors.toList());
            result.setNavs(navVos);
            result.setAttrIds(arrIds);
        }

        // 面包屑-品牌 brandId=8&brandId=9
        if (!CollectionUtils.isEmpty(param.getBrandId())) {
            String queryString = param.getQueryString();
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setNavName("品牌");
            R r = productFeiginService.brandInfos(param.getBrandId());
            if (r.getCode() == 0) {
                List<BrandVo> brandVos = r.getData("brand", new TypeReference<List<BrandVo>>() {
                });
                String value = String.join("、", brandVos.stream().map(BrandVo::getName).collect(Collectors.toList()));
                navVo.setNavValue(value);
            } else {
                navVo.setNavValue(String.join("、", param.getBrandId().stream().map(it -> String.valueOf(it)).collect(Collectors.toList())));
            }
            // ?brandId=8&brandId=9
            for (Long brandId : param.getBrandId()) {
                queryString = queryString.replace("&brandId=" + brandId, "")
                        .replace("brandId=" + brandId, "");
            }
            if (queryString.startsWith("&")) {
                queryString = queryString.substring(1);
            }
            if (StringUtils.isEmpty(queryString)) {
                navVo.setLink(ElasticConstant.SEARCH_URL_1 + queryString);
            } else {
                navVo.setLink(ElasticConstant.SEARCH_URL_2 + queryString);
            }

            result.getNavs().add(navVo);
        }

        // todo 面包屑-分类

        return result;
    }
}
