package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.TeachplanMedia;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TeachplanMediaTest {

    @Autowired
    TeachplanMediaRepository teachplanMediaRepository;


    @Test
    public void findTeachplanMediaTest(){
        Optional<TeachplanMedia> optionalTeachplanMedia = teachplanMediaRepository.findById("4028e58161bd3b380161bd3fe9220008");
        System.out.println(optionalTeachplanMedia);

    }

}
