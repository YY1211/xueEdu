package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsSite;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by Administrator on 2019/3/29.
 */
public interface cmsSiteRepository extends MongoRepository<CmsSite,String> {


}
