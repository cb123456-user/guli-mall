package com.cb.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.cb.common.to.es.SkuEsModel;
import com.cb.gulimall.search.config.ElasticSearchConfig;
import com.cb.gulimall.search.constant.ElasticConstant;
import com.cb.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModelList) throws IOException {

        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel esModel : skuEsModelList) {
            IndexRequest indexRequest = new IndexRequest(ElasticConstant.PRODUCT_INDEX);
            String str = JSON.toJSONString(esModel);
            indexRequest.id(esModel.getSkuId().toString());
            indexRequest.source(str, XContentType.JSON);
            bulkRequest.add(indexRequest);
        }

        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, ElasticSearchConfig.COMMON_OPTIONS);

        boolean result = bulkResponse.hasFailures();

        // todo 如果上架失败，怎么处理
        List<String> collect = Arrays.stream(bulkResponse.getItems())
                .map(BulkItemResponse::getId)
                .collect(Collectors.toList());
        log.info("商品上架成功: {}", collect);


        return !result;
    }
}
