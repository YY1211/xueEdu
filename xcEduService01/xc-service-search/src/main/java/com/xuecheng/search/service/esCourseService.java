package com.xuecheng.search.service;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMedia;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.xml.builders.BooleanQueryBuilder;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class esCourseService {

    @Value("${xuecheng.course.type}")
    private String type;
    @Value("${xuecheng.media.type}")
    private String media_type;

    @Value("${xuecheng.course.index}")
    private String index;
    @Value("${xuecheng.media.index}")
    private String media_index;

    @Value("${xuecheng.course.source_field}")
    private String source_field;
    @Value("${xuecheng.media.source_field}")
    private String media_source_field;

    @Autowired
    RestHighLevelClient restHighLevelClient;
    /**
     * 课程搜索
     * @param page
     * @param size
     * @param courseSearchParam
     * @return
     */
    public QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam) {


        //创建搜索请求对象
        SearchRequest searchRequest = new SearchRequest(index);
        //设置搜索类型
        searchRequest.types(type);
        //创建搜索源配置对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //配置过滤源字段
        String[] source_field_array = source_field.split(",");
        searchSourceBuilder.fetchSource(source_field_array,new String[]{});
        //创建布尔查询对象
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //搜索条件
        //根据关键字搜索
        if(StringUtils.isNotEmpty(courseSearchParam.getKeyword())){
            //创建多字段匹配对象
            MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(courseSearchParam.getKeyword(),"name","description","teachplan").minimumShouldMatch("70%").field("name",10);
            boolQueryBuilder.must(multiMatchQueryBuilder);
        }
        if(StringUtils.isNotEmpty(courseSearchParam.getMt())){
            //根据一级分类
            boolQueryBuilder.filter(QueryBuilders.termQuery("mt",courseSearchParam.getMt()));
        }
        if(StringUtils.isNotEmpty(courseSearchParam.getSt())){
            //根据二级分类
            boolQueryBuilder.filter(QueryBuilders.termQuery("st",courseSearchParam.getSt()));
        }
        if(StringUtils.isNotEmpty(courseSearchParam.getGrade())){
            //根据难度等级
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade",courseSearchParam.getGrade()));
        }

        //设置分页
        if(page<=0){
            page = 1;
        }
        if(size<=0){
            size = 8;
        }
        int from = (page-1)*size;
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);

        //添加布尔查询对象
        searchSourceBuilder.query(boolQueryBuilder);
        //搜索请求对象添加搜索源
        searchRequest.source(searchSourceBuilder);
        //分页查询对象
        QueryResult<CoursePub> queryResult = new QueryResult();
        ArrayList<CoursePub> list = new ArrayList<>();

        try {
            //执行搜索
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
            //获取响应结果
            SearchHits hits = searchResponse.getHits();
            //匹配的总记录数
            long totalHits = hits.totalHits;
            queryResult.setTotal(totalHits);
            //得到匹配到高的文档
            SearchHit[] searchHits = hits.getHits();

            for (SearchHit hit: searchHits){
                CoursePub coursePub = new CoursePub();
                //源文档
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                String id = (String) sourceAsMap.get("id");
                coursePub.setId(id);
                coursePub.setName((String) sourceAsMap.get("name"));
                coursePub.setPic((String) sourceAsMap.get("pic"));
                coursePub.setPrice(sourceAsMap.get("price")!=null? (Double) sourceAsMap.get("price") :null);
                coursePub.setPrice_old(sourceAsMap.get("price_old")!=null? (Double) sourceAsMap.get("price_old") :null);
                list.add(coursePub);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        queryResult.setList(list);
        QueryResponseResult<CoursePub> queryResponseResult = new QueryResponseResult<>(CommonCode.SUCCESS, queryResult);
        return queryResponseResult;
    }

    //使用es的客户端向es请求查询索引信息
    public Map<String,CoursePub> getall(String id){
        //创建搜索请求对象 指定索引
        SearchRequest searchRequest = new SearchRequest(index);
        //指定搜索类型
        searchRequest.types(type);
        //定义searchSourceBuilder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置使用termQuery精确匹配
        searchSourceBuilder.query(QueryBuilders.termQuery("id",id));
        searchRequest.source(searchSourceBuilder);
        HashMap<String , CoursePub> map = new HashMap<>();

        try {
            SearchResponse search = restHighLevelClient.search(searchRequest);
            SearchHits hits = search.getHits();
            SearchHit[] searchHits = hits.getHits();
            for(SearchHit hit : searchHits){
                CoursePub coursePub = new CoursePub();
                //获取源文档的内容
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                //课程id
                String courseId = (String) sourceAsMap.get("id");
                String name = (String) sourceAsMap.get("name");
                String grade = (String) sourceAsMap.get("grade");
                String charge = (String) sourceAsMap.get("charge");
                String pic = (String) sourceAsMap.get("pic");
                String description = (String) sourceAsMap.get("description");
                String teachplan = (String) sourceAsMap.get("teachplan");
                coursePub.setId(courseId);
                coursePub.setName(name);
                coursePub.setPic(pic);
                coursePub.setGrade(grade);
                coursePub.setCharge(charge);
                coursePub.setTeachplan(teachplan);
                coursePub.setDescription(description);
                map.put(courseId,coursePub);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    //根据多个课程计划查询课程媒资信息
    public QueryResponseResult<TeachplanMediaPub> getmedia(String[] teachplanIds) {
        //创建搜索请求对象 指定索引
        SearchRequest searchRequest = new SearchRequest(media_index);
        //指定搜索类型
        searchRequest.types(media_type);
        //定义searchSourceBuilder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置使用termQuery精确匹配
        searchSourceBuilder.query(QueryBuilders.termsQuery("teachplan_id",teachplanIds));

        //过滤字段
        String[] split = media_source_field.split(",");
        searchSourceBuilder.fetchSource(split,new String[]{});
        searchRequest.source(searchSourceBuilder);
        //使用es客户端进行搜索请求Es
        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
        long total = 0;
        //执行搜索
        try {
            SearchResponse search = restHighLevelClient.search(searchRequest);
            SearchHits hits = search.getHits();
            total = hits.getTotalHits();
            SearchHit[] hitsHits = hits.getHits();
            for (SearchHit hit : hitsHits){
                TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                //取出课程计划媒资信息
                String courseid = (String) sourceAsMap.get("courseid");
                String media_id = (String) sourceAsMap.get("media_id");
                String media_url = (String) sourceAsMap.get("media_url");
                String teachplan_id = (String) sourceAsMap.get("teachplan_id");
                String media_fileoriginalname = (String) sourceAsMap.get("media_fileoriginalname");

                teachplanMediaPub.setCourseId(courseid);
                teachplanMediaPub.setMediaUrl(media_url);
                teachplanMediaPub.setMediaFileOriginalName(media_fileoriginalname);
                teachplanMediaPub.setMediaId(media_id);
                teachplanMediaPub.setTeachplanId(teachplan_id);
                teachplanMediaPubList.add(teachplanMediaPub);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        //数据集合
        QueryResult<TeachplanMediaPub> queryResult = new QueryResult<>();
        queryResult.setList(teachplanMediaPubList);
        queryResult.setTotal(total);
        QueryResponseResult<TeachplanMediaPub> queryResponseResult = new QueryResponseResult<>(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;

    }
}
