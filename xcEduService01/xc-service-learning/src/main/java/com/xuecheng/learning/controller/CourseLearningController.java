package com.xuecheng.learning.controller;

import com.xuecheng.api.learning.CourseLearningControllerApi;
import com.xuecheng.framework.domain.learning.response.GetMediaResult;
import com.xuecheng.framework.domain.learning.response.ResponseLearnStatus;
import com.xuecheng.learning.service.LearningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/learning")
public class CourseLearningController implements CourseLearningControllerApi {
    @Autowired
    LearningService learningService;
    @Override
    @GetMapping("/course/getmedia/{courseId}/{teachplanId}")
    public GetMediaResult getmedia(@PathVariable String courseId, @PathVariable String teachplanId) {

        return learningService.getmedia(courseId,teachplanId);
    }

    @GetMapping("/choosecourse/learnstatus/{courseId}")
    public ResponseLearnStatus queryLearnstatus(@PathVariable String courseId){
        return learningService.getLearnStatusByCourseId(courseId);
    }





}
