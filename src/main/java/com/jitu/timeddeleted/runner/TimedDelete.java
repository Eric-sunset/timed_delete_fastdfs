package com.jitu.timeddeleted.runner;

import com.jitu.timeddeleted.controller.RecoRecordController;
import com.jitu.timeddeleted.controller.TaskController;
import jdk.nashorn.internal.runtime.logging.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

/**
 * 随项目启动一同启动的类，主要用作redis数据的清理等
 * @author wangronglian
 * @date 2019年12月20日15:41:41
 * @version 1.0
 */
@Component
@Logger
public class TimedDelete implements ApplicationRunner {
    @Autowired
    RecoRecordController recoRecordController;

    /**
     * 日志对象
     **/
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TimedDelete.class);

    @Autowired
    RedisTemplate redisTemplate;

    @Value("${deleteDays}")
    private Integer deleteDays;

    /**
     * 是否开启默认的定时器
     */
    @Value("${start.schedu}")
    private boolean startSchedu;

    @Value("${delete.version}")
    private String version;

    /**
     * 默认定时间的corn表达式
     * @param args
     * @throws Exception
     */
    @Value("${start.schedu.corn}")
    private String startCorn;

    @Autowired
    TaskController taskController;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            // 清除保存数据的redis键
            Set<String> keys = redisTemplate.keys("*");
            redisTemplate.delete(keys);

            logger.info("The runner start width project");
            logger.info("The project version:"+version);

            // 随项目启动，删除当前系统日期前配置天数的的数据fastDFS
            // start
            /*LocalDate date = LocalDate.now();
            date = date.minusDays(deleteDays);

            logger.info("start delete date,before "+deleteDays+" days:"+date);
            Thread.sleep(2000);

            recoRecordController.deleteFastImage(date.toString(),null);*/
            // end

            // 检测是否开启默认的
            /*if(startSchedu){
                logger.info("The scheduled task started with the project has been configured, and will be configured soon.");
                Thread.sleep(2000);
                try {
                    taskController.start(startCorn);
                }catch (Exception e){
                    logger.info("Error in corn expression format, please confirm the expression format! Then restart the project.");
                }
            }*/
        }catch (Exception e){
            logger.info("delete failed:\n"+e.getMessage());
        }

    }
}
