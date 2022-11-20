package com.cb.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.cb.gulimall.search.config.ElasticSearchConfig;
import lombok.Data;
import lombok.ToString;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Data
    @ToString
    static class Account {
        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
    }

    /**
     * {
     *   "query": {
     *     "match": {
     *       "address": "mill"
     *     }
     *   },
     *   "aggs": {
     *     "ageAgg": {
     *       "terms": {
     *         "field": "age",
     *         "size": 10
     *       }
     *     },
     *     "blanceAvg": {
     *       "avg": {
     *         "field": "balance"
     *       }
     *     }
     *   }
     * }
     */
    @Test
    public void search() throws IOException {

        // 1、构建查询
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // query条件
        searchSourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));
        // age聚合
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        searchSourceBuilder.aggregation(ageAgg);
        // 平均薪资聚合
        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
        searchSourceBuilder.aggregation(balanceAvg);
        System.out.println(searchSourceBuilder);
        searchRequest.source(searchSourceBuilder);
        // 2、查询
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(searchResponse);
        // 3、解析结果
        // hit解析
        SearchHits hits = searchResponse.getHits();
        for (SearchHit hit : hits) {
            String source = hit.getSourceAsString();
            Account account = JSON.parseObject(source, Account.class);
            System.out.println(account);
        }
        // age聚合
        Aggregations aggregations = searchResponse.getAggregations();
        Terms age = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : age.getBuckets()) {
            System.out.println("age: "+ bucket.getKey() + "==>" + bucket.getDocCount());
        }
        // 平均薪资
        Avg balance = aggregations.get("balanceAvg");
        System.out.println("平均薪资: " + balance.getValue());
    }

    @Test
    public void index() throws IOException {
        IndexRequest request = new IndexRequest();
        User user = new User();
        user.setUserName("张三");
        user.setAge(20);
        String str = JSON.toJSONString(user);
        request.index("user").id("1").source(str, XContentType.JSON);
        IndexResponse response = restHighLevelClient.index(request, ElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(response);
    }

    @Data
    class User {
        private String userName;
        private Integer age;
    }

    @Test
    public void contextLoads() {
        System.out.println(restHighLevelClient);
    }

}
