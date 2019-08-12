package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.framework.domain.system.request.QueryDicRequest;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_cms.dao.SysDictionaryRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SysDictionaryService {

    @Autowired
    SysDictionaryRepository sysDictionaryRepository;

    //根据字典类型type查询字典信息
    public SysDictionary findDictionaryByType(String type){
        return sysDictionaryRepository.findByDType(type);
    }

    public QueryResponseResult findList(int page, int size, QueryDicRequest queryDicRequest) {
/*        if(queryDicRequest == null){
            queryDicRequest = new QueryDicRequest();
        }*/
/*
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withMatcher("dName", ExampleMatcher.GenericPropertyMatchers.contains());

*/

        //条件值对象
//        SysDictionary sysDictionary = new SysDictionary();

        //设置条件值
/*        if(StringUtils.isNotEmpty(queryDicRequest.getDName())){
            sysDictionary.setDName(queryDicRequest.getDName());
        }*/
/*        //设置条件值
        if(StringUtils.isNotEmpty(queryDicRequest.getD_Type())){
            sysDictionary.setDType(queryDicRequest.getD_Type());
        }*/

        //定义条件对象Example
//        Example<SysDictionary> example = Example.of(sysDictionary,exampleMatcher);
//        Example<SysDictionary> example = Example.of(sysDictionary);

        //分页参数
        if(page <=0){
            page = 1;
        }
        page = page -1;
        if(size<=0){
            size = 10;
        }
        Pageable pageable = PageRequest.of(page,size);

        Page<SysDictionary> all = sysDictionaryRepository.findAll(pageable);

        QueryResult queryResult = new QueryResult();
        queryResult.setList(all.getContent());//数据列表
        queryResult.setTotal(all.getSize());//数据总记录数
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;
    }
}
