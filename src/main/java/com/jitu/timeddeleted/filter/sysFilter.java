package com.jitu.timeddeleted.filter;

import com.alibaba.fastjson.JSON;
import com.jitu.timeddeleted.controller.RecoRecordController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 系统登录用的Filter
 * @author  wangronglian
 * @date 2019年12月23日09:52:13
 * @version 1.0
 */
@Component
public class sysFilter implements  Filter{
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 日志对象
     **/
    private static final Logger logger = LoggerFactory.getLogger(sysFilter.class);

    /**
     * 直接放行的url,比如登录页
     */
    private static List<String> passUrls = null;

    private static String[] passStaticSffix = {"css","image","js","bootstrap"};

    static {
        passUrls = new ArrayList<>();
        passUrls.add("/".toLowerCase());
        passUrls.add("/login".toLowerCase());
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // 获得当前的request
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession session = request.getSession();
        response.setContentType("text/html;charset=UTF-8");
        // 获得当前请求url
        String url = request.getServletPath();
        String method = request.getMethod();
        PrintWriter out = null;

        // testLogin部分前台页面已移除
        if(url.indexOf("testLogin") == -1){
            // 救活redis中存放的用户信息
            Object usernameObj = session.getAttribute("userInfo");
            if(usernameObj != null){
                redisTemplate.opsForValue().set("userInfo", usernameObj.toString(),5*60, TimeUnit.SECONDS);
            }
        }

        // 测试环境，全部放行，
        //filterChain.doFilter(servletRequest, servletResponse);

        //放行静态资源
        for(int i=0;i<passStaticSffix.length;i++){
            if(url.indexOf(passStaticSffix[i])!=-1){
                filterChain.doFilter(servletRequest, servletResponse);
                // 当前请求放行之后，直接退出方法执行，否则response会提交两次
                return;
            }
        }


        // 从redis中获取用户信息
        Object userInfo = redisTemplate.opsForValue().get("userInfo");
        if (passUrls.contains(url.toLowerCase())) {
            // 当前根目录
            filterChain.doFilter(servletRequest, servletResponse);
        }else if(userInfo==null){
            // 未登录
            logger.info("登录信息失效，请重新登录");
            response.sendRedirect("/");
        }else{
            // 登录过，直接放行
            filterChain.doFilter(servletRequest, servletResponse);
        }

    }
}
