package com.xuecheng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author Administrator
 * @version 1.0
 **/
@Service
public class CourseService {

    @Autowired
    TeachplanRepository teachplanRepository;

    @Autowired
    CourseBaseRepository courseBaseRepository;

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    CoursePicRepository coursePicRepository;

    @Autowired
    CourseMarketRepository courseMarketRepository;

    @Autowired
    CoursePubRepository coursePubRepository;

    @Autowired
    TeachplanMediaRepository teachplanMediaRepository;
    @Autowired
    TeachplanMediaPubRepository teachplanMediaPubRepository;
    @Autowired
    CourseMapper courseMapper;
    @Autowired
    CmsPageClient cmsPageClient;


    @Value("${course-publish.dataUrlPre}")
    private String publish_dataUrlPre;
    @Value("${course-publish.pagePhysicalPath}")
    private String publish_page_physicalpath;
    @Value("${course-publish.pageWebPath}")
    private String publish_page_webpath;
    @Value("${course-publish.siteId}")
    private String publish_siteId;
    @Value("${course-publish.templateId}")
    private String publish_templateId;
    @Value("${course-publish.previewUrl}")
    private String previewUrl;

    //课程计划查询
    public TeachplanNode findTeachplanList(String courseId){
        return teachplanMapper.selectList(courseId);
    }

    //添加课程计划
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan) {
        if(teachplan == null ||
                StringUtils.isEmpty(teachplan.getCourseid()) ||
                StringUtils.isEmpty(teachplan.getPname())){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //课程id
        String courseid = teachplan.getCourseid();
        //页面传入的parentId
        String parentid = teachplan.getParentid();
        if(StringUtils.isEmpty(parentid)){
            //取出该课程的根结点
            parentid = this.getTeachplanRoot(courseid);
        }
        Optional<Teachplan> optional = teachplanRepository.findById(parentid);
        Teachplan parentNode = optional.get();
        //父结点的级别
        String grade = parentNode.getGrade();
        //新结点
        Teachplan teachplanNew = new Teachplan();
        //将页面提交的teachplan信息拷贝到teachplanNew对象中
        BeanUtils.copyProperties(teachplan,teachplanNew);
        teachplanNew.setParentid(parentid);
        teachplanNew.setCourseid(courseid);
        if(grade.equals("1")){
            teachplanNew.setGrade("2");//级别，根据父结点的级别来设置
        }else{
            teachplanNew.setGrade("3");
        }

        teachplanRepository.save(teachplanNew);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    //查询课程的根结点，如果查询不到要自动添加根结点
    private String getTeachplanRoot(String courseId){
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if(!optional.isPresent()){
            return null;
        }
        //课程信息
        CourseBase courseBase = optional.get();
        //查询课程的根结点
        List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndParentid(courseId, "0");
        if(teachplanList == null || teachplanList.size()<=0){
            //查询不到，要自动添加根结点
            Teachplan teachplan = new Teachplan();
            teachplan.setParentid("0");
            teachplan.setGrade("1");
            teachplan.setPname(courseBase.getName());
            teachplan.setCourseid(courseId);
            teachplan.setStatus("0");
            teachplanRepository.save(teachplan);
            return teachplan.getId();
        }
        //返回根结点id
        return teachplanList.get(0).getId();

    }

    //向课程管理数据添加课程与图片的关联信息
    @Transactional
    public ResponseResult addCoursePic(String courseId, String pic) {
        //课程图片信息
        CoursePic coursePic = null;
        //查询课程图片
        Optional<CoursePic> picOptional = coursePicRepository.findById(courseId);
        if(picOptional.isPresent()){
            coursePic = picOptional.get();
        }
        if(coursePic == null){
            coursePic  = new CoursePic();
        }
        coursePic.setPic(pic);
        coursePic.setCourseid(courseId);
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //查询课程图片
    public CoursePic findCoursePic(String courseId) {
        //查询课程图片
        Optional<CoursePic> picOptional = coursePicRepository.findById(courseId);
        if(picOptional.isPresent()){
            CoursePic coursePic = picOptional.get();
            return coursePic;
        }
        return null;
    }

    //删除课程图片
    @Transactional
    public ResponseResult deleteCoursePic(String courseId) {
        //执行删除
        long result = coursePicRepository.deleteByCourseid(courseId);
        if(result>0){
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    //查询课程视图，包括基本信息、图片、营销、课程计划
    public CourseView getCoruseView(String id) {
        CourseView courseView= new CourseView();

        //查询课程基本信息
        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(id);
        if(courseBaseOptional.isPresent()){
            CourseBase courseBase = courseBaseOptional.get();
            courseView.setCourseBase(courseBase);
        }
        //查询课程图片
        Optional<CoursePic> picOptional = coursePicRepository.findById(id);
        if(picOptional.isPresent()){
            CoursePic coursePic = picOptional.get();
            courseView.setCoursePic(coursePic);
        }

        //课程营销信息
        Optional<CourseMarket> marketOptional = courseMarketRepository.findById(id);
        if(marketOptional.isPresent()){
            CourseMarket courseMarket = marketOptional.get();
            courseView.setCourseMarket(courseMarket);
        }

        //课程计划信息
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        courseView.setTeachplanNode(teachplanNode);

        return courseView;

    }

    //根据id查询课程基本信息
    public CourseBase findCourseBaseById(String courseId){
        Optional<CourseBase> baseOptional = courseBaseRepository.findById(courseId);
        if(baseOptional.isPresent()){
            CourseBase courseBase = baseOptional.get();
            return courseBase;
        }
        ExceptionCast.cast(CourseCode.COURSE_DENIED_DELETE);
        return null;
    }
    //课程预览
  public CoursePublishResult preview(String id) {
        //查询课程
        CourseBase courseBaseById = this.findCourseBaseById(id);
        //请求cms添加页面
        //准备cmsPage信息
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);//站点id
        cmsPage.setDataUrl(publish_dataUrlPre+id);//数据模型url
        cmsPage.setPageName(id+".html");//页面名称
        cmsPage.setPageAliase(courseBaseById.getName());//页面别名，就是课程名称
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);//页面物理路径
        cmsPage.setPageWebPath(publish_page_webpath);//页面webpath
        cmsPage.setTemplateId(publish_templateId);//页面模板id

        //远程调用cms
        CmsPageResult cmsPageResult = cmsPageClient.saveCmsPage(cmsPage);
        if(!cmsPageResult.isSuccess()){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }

        CmsPage cmsPage1 = cmsPageResult.getCmsPage();
        String pageId = cmsPage1.getPageId();
        //拼装页面预览的url
        String url = previewUrl+pageId;
        //返回CoursePublishResult对象（当中包含了页面预览的url）
        return new CoursePublishResult(CommonCode.SUCCESS,url);
    }

    /**
     * 课程发布
     * @param id
     * @return
     */
    @Transactional
    public CoursePublishResult publish(String id) {
        //查询课程
        CourseBase courseBaseById = this.findCourseBaseById(id);

        //准备页面信息
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);//站点id
        cmsPage.setDataUrl(publish_dataUrlPre+id);//数据模型url
        cmsPage.setPageName(id+".html");//页面名称
        cmsPage.setPageAliase(courseBaseById.getName());//页面别名，就是课程名称
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);//页面物理路径
        cmsPage.setPageWebPath(publish_page_webpath);//页面webpath
        cmsPage.setTemplateId(publish_templateId);//页面模板id
        //调用cms一键发布接口将课程详情页面发布到服务器
        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
        if(!cmsPostPageResult.isSuccess()){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }

        //保存课程的发布状态为“已发布”
        CourseBase courseBase = this.saveCoursePubState(id);
        if(courseBase == null){
            return new CoursePublishResult(CommonCode.FAIL,null);
        }

        //保存课程索引信息
        CoursePub coursePub = createCoursePub(id);
        saveCoursePub(id, coursePub);

        //缓存课程的信息
        //...todo

        //得到页面的url
        String pageUrl = cmsPostPageResult.getPageUrl();

        //向teachplanMediaPub中保存课程媒资信息
        saveTeachplanMediaPub(id);
        return new CoursePublishResult(CommonCode.SUCCESS,pageUrl);

    }

    //保存课程计划媒资信息
    private void saveTeachplanMediaPub(String courseId){
        //查询课程媒资信息
        List<TeachplanMedia> teachplanMediaList =  teachplanMediaRepository.findByCourseId(courseId);
        //删除课程计划媒资信息存储待索引表
        teachplanMediaPubRepository.deleteByCourseId(courseId);
        //创建待索引表的集合
        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
        //遍历课程媒资信息 插入到待索引表
        for (TeachplanMedia teachplanMedia: teachplanMediaList){
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            BeanUtils.copyProperties(teachplanMedia,teachplanMediaPub);
            teachplanMediaPubList.add(teachplanMediaPub);
        }
        teachplanMediaPubRepository.saveAll(teachplanMediaPubList);
    }


    //创建coursepub对象
    private CoursePub createCoursePub(String id){
        CoursePub coursePub = new CoursePub();
        //根据id查询course_base
        Optional<CourseBase> baseOptional = courseBaseRepository.findById(id);
        if(baseOptional.isPresent()){
            CourseBase courseBase = baseOptional.get();
            BeanUtils.copyProperties(courseBase,coursePub);
        }
        //根据id查询course_pic
        Optional<CoursePic> basePic = coursePicRepository.findById(id);
        if(basePic.isPresent()){
            CoursePic coursePic = basePic.get();
            BeanUtils.copyProperties(coursePic,coursePub);
        }
        //根据id查询课程营销信息
        Optional<CourseMarket> marketOptional = courseMarketRepository.findById(id);
        if(marketOptional.isPresent()){
            CourseMarket courseMarket = marketOptional.get();
            BeanUtils.copyProperties(courseMarket,coursePub);
        }

        //课程计划信息
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        String jsonString = JSON.toJSONString(teachplanNode);
        coursePub.setTeachplan(jsonString);

        return coursePub;
    }
    //将coursepub保存到数据库
    private CoursePub saveCoursePub(String id,CoursePub coursePub){
        //根据课程id查询coursePub
        CoursePub coursePubNew = null;
        Optional<CoursePub> optionalCoursePub = coursePubRepository.findById(id);
        if(optionalCoursePub.isPresent()){
            coursePubNew = optionalCoursePub.get();
        }else {
            coursePubNew = new CoursePub();
        }
        BeanUtils.copyProperties(coursePub,coursePubNew);
        coursePubNew.setId(id);
        //时间戳
        coursePubNew.setTimestamp(new Date());
        //发布时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        String date = simpleDateFormat.format(new Date());
        coursePubNew.setPubTime(date);
        coursePubRepository.save(coursePubNew);
        return coursePubNew;
    }
    /**
     * 更新课程状态为已发布 202002
     * @param courseId
     * @return
     */
    private CourseBase saveCoursePubState(String courseId) {
        CourseBase courseBaseById = this.findCourseBaseById(courseId);
        courseBaseById.setStatus("202002");
        courseBaseRepository.save(courseBaseById);
        return courseBaseById;
    }

    /**
     * 查询课程列表
     * @param page
     * @param size
     * @param courseListRequest
     * @return
     */
    public QueryResponseResult findCourseListPage(String companyId,int page, int size, CourseListRequest courseListRequest) {
        if(courseListRequest == null){
            courseListRequest = new CourseListRequest();
        }
        //将公司id参数传入dao
        courseListRequest.setCompanyId(companyId);
        PageHelper.startPage(page,size);
        Page<CourseInfo> courseListPage = courseMapper.findCourseListPage(courseListRequest);
        int courseCount = courseMapper.findCourseCount();
        List<CourseInfo> result = courseListPage.getResult();
        QueryResult<CourseInfo> queryResult = new QueryResult<CourseInfo>();
        queryResult.setList(result);
        queryResult.setTotal(courseCount);
        return new QueryResponseResult(CommonCode.SUCCESS,queryResult);
    }

    //添加课程提交
    @Transactional
    public AddCourseResult addCourseBase(CourseBase courseBase){
        //课程状态默认未发布
        courseBase.setStatus("202001");
        courseBaseRepository.save(courseBase);

        return new AddCourseResult(CommonCode.SUCCESS,courseBase.getId());
    }

    //查询课程信息
    public CourseBase getCourseBaseById(String courseId) {
        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(courseId);
        if(courseBaseOptional.isPresent()){
            return courseBaseOptional.get();
        }
        return null;
    }

    //更新课程信息
    @Transactional
    public ResponseResult updateCourseBase(String id,CourseBase courseBase) {
        CourseBase course = this.getCourseBaseById(id);
        if(course == null){
            ExceptionCast.cast(CourseCode.COURSE_GET_NOTEXISTS);
        }
            //修改课程信息
            course.setName(courseBase.getName());
            course.setMt(courseBase.getMt());
            course.setSt(courseBase.getSt());
            course.setGrade(courseBase.getGrade());
            course.setStudymodel(courseBase.getStudymodel());
            course.setUsers(courseBase.getUsers());
            course.setDescription(courseBase.getDescription());
            CourseBase save = courseBaseRepository.save(course);
            return new ResponseResult(CommonCode.SUCCESS);
    }

    public CourseMarket getCourseMarketById(String courseId) {
        Optional<CourseMarket> courseMarket = courseMarketRepository.findById(courseId);
        if(courseMarket.isPresent()){
            return courseMarket.get();
        }
        return null;

    }

    public CourseMarket updateCourseMarket(String id, CourseMarket courseMarket) {
        CourseMarket courseMarket1 = this.getCourseMarketById(id);
        if(courseMarket1 != null){
            courseMarket1.setCharge(courseMarket.getCharge());
            courseMarket1.setStartTime(courseMarket.getStartTime());//课程有效期，开始时间
            courseMarket1.setEndTime(courseMarket.getEndTime());//课程有效期，结束时间
            courseMarket1.setPrice(courseMarket.getPrice());
            courseMarket1.setQq(courseMarket.getQq());
            courseMarket1.setValid(courseMarket.getValid());
            courseMarketRepository.save(courseMarket1);

        }else{
            //添加课程信息
            courseMarket1 = new CourseMarket();
            BeanUtils.copyProperties(courseMarket,courseMarket1);
            courseMarket1.setId(id);
            courseMarketRepository.save(courseMarket1);
        }

        return courseMarket1;
    }

    /**
     * 保存课程计划与视频关系
     * @param teachplanMedia
     * @return
     */
    public ResponseResult savemdia(TeachplanMedia teachplanMedia) {
        //查询课程计划视频表
        if(teachplanMedia == null || StringUtils.isEmpty(teachplanMedia.getTeachplanId())){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //课程计划
        String teachplanId = teachplanMedia.getTeachplanId();
        Optional<Teachplan> optional  = teachplanRepository.findById(teachplanId);

        if(!optional.isPresent()){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //查询到教学计划
        Teachplan teachplan = optional.get();
        //取出等级
        String grade = teachplan.getGrade();
        if(StringUtils.isEmpty(grade) || !grade.equals("3")){
            //只允许选择第三级的课程计划关联视频
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_GRADEERROR);
        }
        
        //查询teachplanMedia
        Optional<TeachplanMedia> mediaOptional = teachplanMediaRepository.findById(teachplanId);
        TeachplanMedia one = null;
        if(mediaOptional.isPresent()){
            one = mediaOptional.get();
        }else{
            one = new TeachplanMedia();
        }

        //将one保存到数据库
        one.setCourseId(teachplan.getCourseid());//课程id
        one.setMediaId(teachplanMedia.getMediaId());//媒资文件的id
        one.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());//媒资文件的原始名称
        one.setMediaUrl(teachplanMedia.getMediaUrl());//媒资文件的url
        one.setTeachplanId(teachplanId);
        teachplanMediaRepository.save(one);

        return new ResponseResult(CommonCode.SUCCESS);
    }
}
