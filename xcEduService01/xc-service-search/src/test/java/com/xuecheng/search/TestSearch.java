package com.xuecheng.search;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestSearch {

    @Autowired
    RestHighLevelClient client;

    @Autowired
    RestClient restClient;


    @Test
    public void test() throws IOException {
        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //搜索方式
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        //设置源字段过滤
        searchSourceBuilder.fetchSource(new String[]{"name","studymodel","price"},new String[]{});
        //指定请求搜索源
        searchRequest.source(searchSourceBuilder);
        //执行搜索
        SearchResponse searchResponse = client.search(searchRequest);
        SearchHits hits = searchResponse.getHits();
        long totalHits = hits.getTotalHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits){
            //文档的主键
            String id = hit.getId();
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            System.out.println(id);
            System.out.println(name);
        }
    }
}
