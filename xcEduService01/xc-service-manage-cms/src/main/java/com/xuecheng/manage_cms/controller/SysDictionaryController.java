package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.cms.SysDictionaryControllerApi;
import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.framework.domain.system.request.QueryDicRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.manage_cms.service.SysDictionaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys/directory")
public class SysDictionaryController implements SysDictionaryControllerApi {

    @Autowired
    SysDictionaryService sysDictionaryService;
    @Override
    @GetMapping("/get/{type}")
    public SysDictionary getByType(@PathVariable("type") String type) {
        return sysDictionaryService.findDictionaryByType(type);
    }

    @GetMapping("/list/{page}/{size}")
    public QueryResponseResult findList(@PathVariable("page") int page, @PathVariable("size")int size, QueryDicRequest queryDicRequest) {
        return sysDictionaryService.findList(page,size,queryDicRequest);
    }


}
