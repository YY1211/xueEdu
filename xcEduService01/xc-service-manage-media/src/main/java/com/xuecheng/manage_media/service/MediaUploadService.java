package com.xuecheng.manage_media.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitmqConfig;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class MediaUploadService {
    @Autowired
    MediaFileRepository mediaFileRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Value("${xc-service-manage-media.upload-location}")
    String uploadPath;
    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    String routingkey_media_video;

    /**文件上传前准备
     * @param fileMd5
     * @param fileName
     * @param fileSize
     * @param mimetype
     * @param fileExt
     * @return
     */
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        //1、得到文件上传目录
        String fileFolderPath = this.getFileFolderPath(fileMd5);
        //2、得到文件路径
        String filePath = this.getFilePath(fileMd5,fileExt);
        File file = new File(filePath);

        //2、检查数据库文件是否存在
        Optional<MediaFile> optionalMediaFile = mediaFileRepository.findById(fileMd5);
        //文件存在返回信息
        if(optionalMediaFile.isPresent()&& file.exists()){
            //文件存在
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }
        //文件不存在 则检查文件所在目录是否存在，如果不存在则创建
        File fileFolder= new File(fileFolderPath);
        if(!fileFolder.exists()){
            fileFolder.mkdirs();
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }


    /**
     * 检查块文件
     * @param fileMd5
     * @param chunk
     * @param chunkSize
     * @return
     */
    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize) {
        //得到文件分块目录
        String chunkFileFolderPath = this.getChunkFolderPath(fileMd5);
        //得到块文件
        File chunkFile = new File(chunkFileFolderPath+chunk);
        if(chunkFile.exists()){
            return new CheckChunkResult(CommonCode.SUCCESS,true);
        }else {
            return new CheckChunkResult(CommonCode.SUCCESS,false);
        }
    }



    /**
     * 上传块文件
     * @param file
     * @param fileMd5
     * @param chunk
     * @return
     */
    public ResponseResult uploadchunk(MultipartFile file, String fileMd5, Integer chunk) {
        //得到文件分块目录
        String chunkFolderPath = this.getChunkFolderPath(fileMd5);
        //得到分块文件
        String chunkFilePath = chunkFolderPath + chunk;

        File fileFolderPath = new File(chunkFolderPath);
        if(!fileFolderPath.exists()){
            fileFolderPath.mkdirs();
        }
        //得到上传文件的输入流
        InputStream inputStream = null;
        FileOutputStream outputStream  =null;
        try {
            inputStream = file.getInputStream();
            outputStream = new FileOutputStream(new File(chunkFilePath));
            IOUtils.copy(inputStream,outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 合并块文件
     * @param fileMd5
     * @param fileName
     * @param fileSize
     * @param mimetype
     * @param fileExt
     * @return
     */
    public ResponseResult mergechunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        //得到分块文件的属目录
        String chunkFileFolderPath = this.getChunkFolderPath(fileMd5);
        File chunkFileFolder = new File(chunkFileFolderPath);

        //分块文件列表
        File[] files = chunkFileFolder.listFiles();
        List<File> fileList = Arrays.asList(files);

        //创建一个合并文件
        String filePath = this.getFilePath(fileMd5, fileExt);
        File mergeFile = new File(filePath);

        //执行合并
        mergeFile = this.mergeFile(fileList, mergeFile);

        if(mergeFile == null){
            //合并文件失败
            ExceptionCast.cast(MediaCode.MERGE_FILE_FAIL);
        }


        //2、校验文件的md5值是否和前端传入的md5一到
        boolean checkFileMd5 = this.checkFileMd5(mergeFile, fileMd5);

        if(!checkFileMd5){
            //校验文件失败
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }
        //将分块文件删除
        deleteChunkFile(chunkFileFolderPath);

        //3、将文件的信息写入mongodb
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileOriginalName(fileName);
        mediaFile.setFileName(fileMd5 + "." +fileExt);
        //文件路径保存相对路径
        String relativeFilePath = fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/";
        mediaFile.setFilePath(relativeFilePath);
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimetype);
        mediaFile.setFileType(fileExt);
        //状态为上传成功
        mediaFile.setFileStatus("301002");
        mediaFile.setProcessStatus("303000");
        mediaFileRepository.save(mediaFile);
        //向mq发消息
//        ResponseResult result = sendProcessVideoMsg(mediaFile.getFileId());
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 向mq发消息
     * @param fileId
     * @return
     */
    public ResponseResult sendProcessVideoMsg(String fileId) {
        Optional<MediaFile> mediaFileOptional = mediaFileRepository.findById(fileId);
                if(!mediaFileOptional.isPresent()){
                    ExceptionCast.cast(CommonCode.FAIL);
                }
        //创建消息对象
        Map<String,String> msg = new HashMap<>();
        msg.put("fileId",fileId);
        //转成json串
        String jsonString = JSON.toJSONString(msg);

        try {
            rabbitTemplate.convertAndSend(RabbitmqConfig.EX_MEDIA_PROCESSTASK,routingkey_media_video,jsonString);
        } catch (AmqpException e) {
            e.printStackTrace();
            return new ResponseResult(CommonCode.FAIL);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 删除分块
     * @param chunkFileFolderPath
     * @return
     */
    private boolean deleteChunkFile(String chunkFileFolderPath) {
        File chunkFileFolder = new File(chunkFileFolderPath);
        File[] files = chunkFileFolder.listFiles();
        if(files!=null){
            for (File file :files){
                /* if(file.isDirectory()){deleteChunkFile(chunkFileFolderPath); }else{ }*/
                file.delete();
            }
        }
        chunkFileFolder.delete();
        return true;
    }


    /**
     * 校验文件的md5值
     * @param mergeFile
     * @param fileMd5
     * @return
     */
    private boolean checkFileMd5(File mergeFile, String fileMd5) {

        try {
            //创建文件输入流
            FileInputStream inputStream = new FileInputStream(mergeFile);
            //得到文件的md5
            String md5Hex = DigestUtils.md5Hex(inputStream);

            //和传入的md5比较
            if(fileMd5.equalsIgnoreCase(md5Hex)){
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }


    /**
     * 合并文件
     * @param chunkFileList
     * @param mergeFile
     * @return
     */
    private File mergeFile(List<File> chunkFileList, File mergeFile) {
        //如果合并文件已存在则删除，否则创建新文件
        try {
        if (mergeFile.exists()) {
            mergeFile.delete();
        } else {
            //创建一个新文件
           mergeFile.createNewFile();
        }

        //对块文件进行排序
        Collections.sort(chunkFileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if(Integer.parseInt(o1.getName())>Integer.parseInt(o2.getName())){
                    return 1;
                }
                return -1;

            }
        });
            //创建一个写对象
            RandomAccessFile raf_write = new RandomAccessFile(mergeFile,"rw");
            byte[] b = new byte[1024];
            for(File chunkFile:chunkFileList){
                RandomAccessFile raf_read = new RandomAccessFile(chunkFile,"r");
                int len = -1;
                while ((len = raf_read.read(b))!=-1){
                    raf_write.write(b,0,len);
                }
                raf_read.close();
            }
            raf_write.close();
            return mergeFile;
            }catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 得到文件分块目录
     * @param fileMd5
     * @return
     */
    private String getChunkFolderPath(String fileMd5) {
        return uploadPath + fileMd5.substring(0,1) + "/" +fileMd5.substring(1,2) + "/" +fileMd5 +"/chunk/";
    }
    /**
     * 得到文件目录
     * @param fileMd5
     * @return
     */
    private String getFileFolderPath(String fileMd5) {
        return uploadPath + fileMd5.substring(0,1) + "/" +fileMd5.substring(1,2) + "/" +fileMd5 + "/";
    }


    /**
     * 得到文件路径
     * @param fileMd5
     * @param fileExt
     * @return
     */
    private String getFilePath(String fileMd5, String fileExt) {
        return uploadPath + fileMd5.substring(0,1) + "/" +fileMd5.substring(1,2) + "/" +fileMd5 + "/" +fileMd5+"."+fileExt;
    }

}
