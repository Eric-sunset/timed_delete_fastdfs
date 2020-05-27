package com.jitu.timeddeleted.service;

import java.util.Map;

/**
 * 登录模块的service接口
 * @author wangronglina
 * @date 2019年12月23日11:37:05
 * @version 1.0
 */
public interface LoginService {
    Map<String, Object> login(Map<String,Object> param);
}
