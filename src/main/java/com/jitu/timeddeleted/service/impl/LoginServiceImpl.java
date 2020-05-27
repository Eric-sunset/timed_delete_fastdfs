package com.jitu.timeddeleted.service.impl;

import com.jitu.timeddeleted.dao.LoginDao;
import com.jitu.timeddeleted.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 用户登录模块的service层实现类
 * @auto wangronglian
 * @date 2019年12月23日11:37:44
 * @version 1.0
 */
@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    LoginDao loginDao;

    @Override
    public Map<String, Object> login(Map<String,Object> param) {
        return loginDao.login(param);
    }
}
