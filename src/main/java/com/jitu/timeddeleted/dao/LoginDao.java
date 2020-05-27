package com.jitu.timeddeleted.dao;

import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

/**
 * 用户登录模块的dao层
 * @author wangronglian
 * @date 2019年12月23日11:38:33
 * @version  1.0
 */

@Mapper
public interface LoginDao {
    public Map<String, Object> login(Map<String,Object> param);
}
