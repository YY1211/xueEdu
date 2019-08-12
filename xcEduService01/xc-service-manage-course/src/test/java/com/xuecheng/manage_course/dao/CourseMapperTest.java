package com.xuecheng.manage_course.dao;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class CourseMapperTest {

    @Autowired
    CourseMapper courseMapper;

    @Test
    public void findCourseListTest(){
        PageHelper.startPage(0,10);
        Page<CourseInfo> courseList = courseMapper.findCourseListPage(null);
        List<CourseInfo> result = courseList.getResult();

        for (CourseInfo courseInfo : result){
            System.out.println(courseInfo.getName()+","+courseInfo.getPic());
        }

    }

    @Test
    public void findCourseCountTest(){
        int courseCount = courseMapper.findCourseCount();
        System.out.println(courseCount);
    }
}
