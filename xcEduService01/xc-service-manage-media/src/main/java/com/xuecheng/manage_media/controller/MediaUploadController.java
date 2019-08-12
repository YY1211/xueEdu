package com.xuecheng.manage_media.controller;

import com.xuecheng.api.media.MediaUploadControllerApi;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.service.MediaUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/media/upload")
public class MediaUploadController implements MediaUploadControllerApi {

    @Autowired
    MediaUploadService mediaUploadService;

    /**
     * 1、上传前检查上传环境
     *  检查文件是否上传，已上传则直接返回。
     *  检查文件上传路径是否存在，不存在则创建。
     * @param fileMd5 文件的md5值
     * @param fileName 文件的名字
     * @param fileSize 文件的大小
     * @param mimetype 文件的类型
     * @param fileExt 文件的扩展名
     * @return
     */
    @Override
    @PostMapping("/register")
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        return mediaUploadService.register(fileMd5,fileName,fileSize,mimetype,fileExt);
    }

    /**
     * 2、分块检查
     *  检查分块文件是否上传，已上传则返回true。
     *  未上传则检查上传路径是否存在，不存在则创建。
     * @param fileMd5
     * @param chunk 分块文件
     * @param chunkSize 分块文件大小
     * @return
     */
    @Override
    @PostMapping("/checkchunk")
    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize) {
        return mediaUploadService.checkchunk(fileMd5,chunk,chunkSize);
    }

    /**
     * 3、分块上传
     * 将分块文件上传到指定的路径
     * @param file 文件对象
     * @param chunk 分块
     * @param fileMd5 文件md5值
     * @return
     */
    @Override
    @PostMapping("/uploadchunk")
    public ResponseResult uploadchunk(MultipartFile file, Integer chunk, String fileMd5) {
        return mediaUploadService.uploadchunk(file,fileMd5,chunk);
    }

    /**
     * 4、合并分块
     * 将所有分块文件合并为一个文件。
     * 在数据库记录文件信息。
     * @param fileMd5 文件md5值
     * @param fileName 文件名字
     * @param fileSize 文件大小
     * @param mimetype 文件类型
     * @param fileExt 文件扩展名
     * @return
     */
    @Override
    @PostMapping("/mergechunks")
    public ResponseResult mergechunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        return mediaUploadService.mergechunks(fileMd5,fileName,fileSize, mimetype,fileExt);
    }
}
