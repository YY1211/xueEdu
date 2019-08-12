package com.xuecheng.api.media;

import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

@Api(value = "媒资管理接口",tags={"媒资管理接口"})
public interface MediaUploadControllerApi {

    /**
     *文件上传注册
     * @param fileMd5 文件的md5值
     * @param fileName 文件的名字
     * @param fileSize 文件的大小
     * @param mimetype 文件的类型
     * @param fileExt 文件的扩展名
     * @return
     */
    @ApiOperation("文件上传注册")
    public ResponseResult register(String fileMd5,
                                   String fileName,
                                   Long fileSize,
                                   String mimetype,
                                   String fileExt);

    /**
     *
     * @param fileMd5
     * @param chunk 分块文件
     * @param chunkSize 分块文件大小
     * @return
     */
    @ApiOperation("分块检查")
    public CheckChunkResult checkchunk(String fileMd5,
                                       Integer chunk,
                                       Integer chunkSize);

    /**
     *
     * @param file 文件对象
     * @param chunk 分块
     * @param fileMd5 文件md5值
     * @return
     */
    @ApiOperation("上传分块")
    public ResponseResult uploadchunk(MultipartFile file,
                                      Integer chunk,
                                      String fileMd5);

    /**
     *
     * @param fileMd5 文件md5值
     * @param fileName 文件名字
     * @param fileSize 文件大小
     * @param mimetype 文件类型
     * @param fileExt 文件扩展名
     * @return
     */
    @ApiOperation("合并文件")
    public  ResponseResult  mergechunks(String  fileMd5,
                                String  fileName,
                                Long  fileSize,
                                String  mimetype,
                                String  fileExt);
}
