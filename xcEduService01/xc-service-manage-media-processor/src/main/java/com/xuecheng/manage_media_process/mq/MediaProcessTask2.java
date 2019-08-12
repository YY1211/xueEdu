//package com.xuecheng.manage_media_process.mq;
//
//import com.alibaba.fastjson.JSON;
//import com.xuecheng.framework.domain.media.MediaFile;
//import com.xuecheng.framework.domain.media.MediaFileProcess_m3u8;
//import com.xuecheng.framework.utils.HlsVideoUtil;
//import com.xuecheng.framework.utils.Mp4VideoUtil;
//import com.xuecheng.manage_media_process.dao.MediaFileRepository;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//@Component
//public class MediaProcessTask2 {
//
//    @Autowired
//    MediaFileRepository mediaFileRepository;
//    @Value("${xc-service-manage-media.mq.ffmpeg-path}")
//    String ffmpegPath;
//    @Value("${xc-service-manage-media.mq.video-location}")
//    String videoLocation;
//
//
//    @RabbitListener(queues = "${xc-service-manage-media.mq.queue-media-video-processor}")
//    public void receiveProcessTask(String msg) {
//        //1、解析消息内容
//        Map map = JSON.parseObject(msg, Map.class);
//        String mediaId = (String) map.get("fileId");
//        //2、拿meidaId从数据库查询文件信息
//        Optional<MediaFile> mediaFileOptional = mediaFileRepository.findById(mediaId);
//        if (!mediaFileOptional.isPresent()) {
//            return;
//        }
//        MediaFile mediaFile = mediaFileOptional.get();
//        //得到文件类型
//        String fileType = mediaFile.getFileType();
//        if (!fileType.equalsIgnoreCase("avi")) {
//            mediaFile.setFileStatus("303004");//无需处理
//            mediaFileRepository.save(mediaFile);
//            return;
//        } else {
//            mediaFile.setFileStatus("303001");//处理中
//            mediaFileRepository.save(mediaFile);
//        }
//        /*****生成MP4文件****/
//        //上传文件的目录
//        String video_path = videoLocation + mediaFile.getFilePath() + mediaFile.getFileName();
//        //要生成的文件
//        String mp4_name = mediaFile.getFileId() + ".mp4";
//        String mp4folder_path = videoLocation + mediaFile.getFilePath();
//        Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpegPath, video_path, mp4_name, mp4folder_path);
//
//        String result = mp4VideoUtil.generateMp4();
//        if (result == null || !result.equals("success")) {
//            //处理失败
//            mediaFile.setFileStatus("303003");
//            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
//            mediaFileProcess_m3u8.setErrormsg("处理失败");
//            mediaFileRepository.save(mediaFile);
//            return;
//        }
//
//        /*将MP4生成m3u8和ts文件*/
//        //String ffmpeg_path, String video_path, String m3u8_name,String m3u8folder_path)
//        Optional<MediaFile> optionalMediaFile = mediaFileRepository.findById(mediaId);
//        if (!optionalMediaFile.isPresent()) {
//            return;
//        }
//        //mp4文件路径
//        String mp4VideoPath = videoLocation + mediaFile.getFilePath() + mp4_name;
//        //m3u8_name文件名称
//        String m3u8_name = mediaFile.getFileId() + ".m3u8";
//        //m3u8文件所在目录
//        String m3u8Folder_path = videoLocation + mediaFile.getFilePath() + "hls/";
//        //生成m3u8和ts文件
//        HlsVideoUtil hlsVideoUtil = new HlsVideoUtil(ffmpegPath, mp4VideoPath, m3u8_name, m3u8Folder_path);
//        String result2 = hlsVideoUtil.generateM3u8();
//
//        if (result == null || !result.equals("success")) {
//            //处理失败
//            mediaFile.setFileStatus("303003");
//            //定义mediaFileProcess_m3u8
//            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
//            //记录失败原因
//            mediaFileProcess_m3u8.setErrormsg(result);
//            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
//            mediaFileRepository.save(mediaFile);
//            return;
//        }
//
//        //处理成功
//        //获取ts文件列表
//        List<String> ts_list = hlsVideoUtil.get_ts_list();
//        mediaFile.setProcessStatus("303002");
//        //定义mediaFileProcess_m3u8
//        MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
//        mediaFileProcess_m3u8.setTslist(ts_list);
//        mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
//        //保存fileUrl（此url就是视频播放的相对路径）
//        String fileUrl = mediaFile.getFilePath() + "hls/" + m3u8_name;
//        mediaFile.setFileUrl(fileUrl);
//        mediaFileRepository.save(mediaFile);
//    }
//}
