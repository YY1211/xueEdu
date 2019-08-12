package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.framework.domain.system.request.QueryDicRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api(value = "数据字典接口",tags = {"提供数据字典接口的管理、查询功能"})
public interface SysDictionaryControllerApi {
    //数据字典
    @ApiOperation(value="数据字典查询接口")
    public SysDictionary getByType(String type);

    //页面查询
    @ApiOperation("分页查询字典列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name="page",value = "页码",required=true,paramType="path",dataType="int"),
            @ApiImplicitParam(name="size",value = "每页记录数",required=true,paramType="path",dataType="int")
    })
    public QueryResponseResult findList(int page, int size, QueryDicRequest queryDicRequest);
}
