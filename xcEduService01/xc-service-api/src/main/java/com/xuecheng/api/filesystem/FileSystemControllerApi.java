package com.xuecheng.api.filesystem;

import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import com.xuecheng.framework.domain.system.request.QueryDicRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by Administrator.
 */
@Api(value="文件管理接口",description = "文件管理接口，提供文件的增、删、改、查")
public interface FileSystemControllerApi {

    //上传文件
    @ApiOperation("上传文件接口")
    public UploadFileResult  upload(MultipartFile multipartFile,
                                    String filetag,
                                    String businesskey,
                                    String metadata);


}
