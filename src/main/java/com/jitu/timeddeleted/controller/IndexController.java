package com.jitu.timeddeleted.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 首页控制跳转的controller层
 * @date 2019年12月6日13:46:05
 * @author  wangronglian
 * @version 1.0
 */
@Controller
public class IndexController {

    /**
     * 通过controller跳转到index首页的HTML页面
     * @return
     */
    @RequestMapping("/")
    public String login(){
//        return  "/config";
        return  "/login";
    }

    @RequestMapping("/index")
    public String index(){
//        return  "/config";
        return  "/index";
    }

    @RequestMapping("/config")
    public  String logionPage(){
        return "/config";
    }

    @RequestMapping("/download")
    public  String download(){
        return "/download";
    }

    /**
     * 专门用来测试，是否是登录状态的接口
     * @return
     */
    @RequestMapping("/testLogin")
    @ResponseBody
    public String testLogin(){
        return "login";
    }

}
