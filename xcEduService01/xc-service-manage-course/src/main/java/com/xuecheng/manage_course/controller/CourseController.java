package com.xuecheng.manage_course.controller;

import com.xuecheng.api.course.CourseControllerApi;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.web.BaseController;
import com.xuecheng.manage_course.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * @author Administrator
 * @version 1.0
 **/
@RestController
@RequestMapping("/course")
public class CourseController extends BaseController implements CourseControllerApi {

    @Autowired
    CourseService courseService;


    //查询课程计划
    //@PreAuthorize("hasAuthority('course_teachplan_list')")
    @Override
    @GetMapping("/teachplan/list/{courseId}")
    public TeachplanNode findTeachplanList(@PathVariable("courseId") String courseId) {
        return courseService.findTeachplanList(courseId);
    }
    //@PreAuthorize("hasAuthority('course_teachplan_add')")
    @Override
    @PostMapping("/teachplan/add")
    public ResponseResult addTeachplan(@RequestBody  Teachplan teachplan) {

        return courseService.addTeachplan(teachplan);
    }

   // @PreAuthorize("hasAuthority('course_add_pic')")
    @Override
    @PostMapping("/coursepic/add")
    public ResponseResult addCoursePic(@RequestParam("courseId") String courseId, @RequestParam("pic")String pic) {
        return courseService.addCoursePic(courseId,pic);
    }


    //查询课程图片
   // @PreAuthorize("hasAuthority('course_find_pic')")
    @Override
    @GetMapping("/coursepic/list/{courseId}")
    public CoursePic findCoursePic(@PathVariable("courseId") String courseId) {
        return courseService.findCoursePic(courseId);
    }

    @Override
    //@PreAuthorize("hasAuthority('course_delete_pic')")
    @DeleteMapping("/coursepic/delete")
    public ResponseResult deleteCoursePic(@RequestParam("courseId") String courseId) {
        return courseService.deleteCoursePic(courseId);
    }

    //课程预览数据模型准备

    /**
     * 通过cms系统url的请求 返回给数据模式
     * @param id
     * @return
     */
    @Override
    @GetMapping("/courseview/{id}")
    public CourseView courseview(@PathVariable("id") String id) {
        return courseService.getCoruseView(id);
    }


    //查询课程列表
    //@PreAuthorize("hasAuthority('course_find_list')")
    @Override
    @GetMapping("/coursebase/list/{page}/{size}")
    public QueryResponseResult findCourseList(@PathVariable("page") int page, @PathVariable("size") int size, CourseListRequest courseListRequest) {
        //获取当前用户信息
//        XcOauth2Util xcOauth2Util = new XcOauth2Util();
//        XcOauth2Util.UserJwt userJwt = xcOauth2Util.getUserJwtFromHeader(request);
        //当前用户所属单位的id
//        String company_id = userJwt.getCompanyId();
        String company_id = "1";
        return courseService.findCourseListPage(company_id,page,size,courseListRequest);
    }

    //添加课程信息
   // @PreAuthorize("hasAuthority('coursebase_add')")
    @Override
    @PostMapping("/coursebase/add")
    public AddCourseResult addCourseBase(@RequestBody CourseBase courseBase) {
        return courseService.addCourseBase(courseBase);
    }

   // @PreAuthorize("hasAuthority('coursebase_get')")
    @Override
    @GetMapping("/coursebase/get/{courseId}")
    public CourseBase getCourseBase(@PathVariable("courseId") String courseId) {
        return courseService.getCourseBaseById(courseId);
    }

   // @PreAuthorize("hasAuthority('coursebase_update')")
    @Override
    @PutMapping("/coursebase/update/{id}")
    public ResponseResult updateCourseBase(@PathVariable("id") String id, @RequestBody CourseBase courseBase) {
        return courseService.updateCourseBase(id,courseBase);
    }

   // @PreAuthorize("hasAuthority('coursemarket_get')")
    @Override
    @GetMapping("/coursemarket/get/{courseId}")
    public CourseMarket getCourseMarketById(@PathVariable("courseId") String courseId) {
        return courseService.getCourseMarketById(courseId);
    }

 //   @PreAuthorize("hasAuthority('coursemarket_update')")
    @Override
    @PutMapping("/coursemarket/update/{id}")
    public ResponseResult updateCourseMarket(@PathVariable("id") String id, @RequestBody CourseMarket courseMarket) {
        CourseMarket courseMarket1 = courseService.updateCourseMarket(id, courseMarket);
        if(courseMarket1 != null){
            return new ResponseResult(CommonCode.SUCCESS);
        }else{
            return new ResponseResult(CommonCode.FAIL);

        }
    }

    /**
     * 课程预览
     * 根据传入的课程 得到课程文件名和别名 组装cmspage信息 再远程调用cms服务保存cmspage 再返回cmspage
     * 从cmspage中得到页面id 拼装页面预览url
     * @param id
     * @return
     */

    @Override
    @PostMapping("/preview/{id}")
    public CoursePublishResult preview(@PathVariable("id") String id) {
        return courseService.preview(id);

    }


    /**
     * 页面发布
     * @param id
     * @return
     */
    @Override
    @PostMapping("/publish/{id}")
    public CoursePublishResult publish(@PathVariable("id") String id) {
        return courseService.publish(id);
    }


    /**
     * 保存课程计划和视频的关联
     * @param teachplanMedia
     * @return
     */
    @Override
    @PostMapping("/savemedia")
    public ResponseResult savemedia(@RequestBody TeachplanMedia teachplanMedia) {
        return courseService.savemdia(teachplanMedia);
    }
}
