package com.xuecheng.api.media;

import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;


@Api(value = "媒资文件管理",tags={"媒资文件管理接口"})
public interface MediaFileControllerApi {

    /**
     *
     * @param page
     * @param size
     * @param queryMediaFileRequest 文件原始名 处理状态 标志
     * @return
     */
    @ApiOperation("查询文件列表")
    public QueryResponseResult findList(int page, int size, QueryMediaFileRequest
            queryMediaFileRequest) ;

}
