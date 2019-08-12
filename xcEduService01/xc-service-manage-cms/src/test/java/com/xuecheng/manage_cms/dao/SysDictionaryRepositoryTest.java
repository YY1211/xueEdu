package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.system.SysDictionary;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class SysDictionaryRepositoryTest {

    @Autowired
    SysDictionaryRepository sysDictionaryRepository;

    @Test
    public void findDictionaryTest(){
        SysDictionary byType = sysDictionaryRepository.findByDType("302");

    }
}
