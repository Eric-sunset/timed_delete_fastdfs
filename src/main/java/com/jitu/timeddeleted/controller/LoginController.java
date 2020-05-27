package com.jitu.timeddeleted.controller;

import com.jitu.timeddeleted.service.LoginService;
import com.jitu.timeddeleted.tool.MD5Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户登录模块controller
 * @wangronglina
 * @date 2019年12月23日11:09:01
 * @version 1.0
 */
@Controller
public class LoginController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    LoginService loginService;

    /**
     * 存放登录用户的表
     */
    @Value("${user.tableName}")
    private String tableName;

    /**
     * 表中存放用户名的字段
     */
    @Value("${user.table.username}")
    private String usernameFiled;

    /**
     * 表中存放密码的字段
     */
    @Value("${user.table.password}")
    private String passwordFiled;

    @RequestMapping("login")
    @ResponseBody
    public String login(@RequestParam(value = "username" ,required = false)String username,
                        @RequestParam(value = "password" ,required = false)String password,
                        ServletRequest servletRequest,
                        HttpSession httpSession){
        try {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            String s = MD5Tool.md5(password);
            // 封装参数
            Map<String,Object> param = new HashMap<>();
            param.put("tableName",tableName);
            param.put("usernameFiled",usernameFiled);
            param.put("passwordFiled",passwordFiled);
            param.put("username","'"+username+"'");
            // 查询用户信息
            Map<String,Object> userInfo  = loginService.login(param);
            if(userInfo == null){
                return "failed";
            }else{
                String password1 = (String) userInfo.get("password");
                if(s.equals(password1)){
                    // 保存用户信息到session和redis中
                    httpSession.setAttribute("userInfo",userInfo.get("username"));
                    redisTemplate.opsForValue().set("userInfo",userInfo.get("username"),5*60, TimeUnit.SECONDS);
                    return "success";
                }else{
                    return "failed";
                }
            }


        }catch (Exception e){
            e.printStackTrace();
        }
        return "success";
    }

    @RequestMapping("logOut")
    public String logOut(HttpSession httpSession){
        httpSession.setAttribute("userInfo",null);
        return "/login";
    }
}
