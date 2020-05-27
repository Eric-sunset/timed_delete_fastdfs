package com.jitu.timeddeleted.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * 控制定时任务的controller
 * @author wangronglian
 * @date 2019年12月20日11:19:12
 * @version 1.0
 */
@RestController
@RequestMapping("task")
public class TaskController {
    @Autowired
    RecoRecordController recoRecordController;

    @Value("${deleteDays}")
    private Integer deleteDays;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    public static Map<String, ScheduledFuture<?>> taskmap=new HashMap<>();

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    /**
     * 开启任务方法，改变任务的方法包含开启的全部东西，因此调用改变任务
     * 要执行的任务方法在最下面的内部类中
     * @param cornString corn表达式
     * @return
     */
    @RequestMapping("/start")
    public String start(String cornString) {

        TaskThread tt=new TaskThread("222");
        ScheduledFuture<?> future=threadPoolTaskScheduler.schedule(tt, new CronTrigger(cornString));
        taskmap.put("task1", future);
        System.out.println("start schedule success!");
        return "start";
    }

    /**
     * 停止任务
     * @return
     */
    @RequestMapping("/stop")
    public String stop() {
        ScheduledFuture<?> future=taskmap.get("task1");
        if (future != null) {
            future.cancel(true);
        }
        System.out.println("stop schedule success!");
        return "success";
    }

    /**
     * 改变任务
     * @param cornString corn表达式
     * @return
     */
    @RequestMapping("/change")
    public String change(String cornString) {

        try {
            stop();// 先停止，在开启.
            ScheduledFuture<?> future=taskmap.get("task1");
            TaskThread tt=new TaskThread( "222");
            future=threadPoolTaskScheduler.schedule(tt, new CronTrigger(cornString));
            taskmap.put("task1", future);
//            System.out.println("DynamicTask.startCron10()");
            System.out.println("change schedule success!");
            return "success";
        }catch (Exception e){
            return "error";
        }
    }


    /**
     * 内部类，执行task的任务
     * @author wangronglian
     * @date 2019年12月20日11:21:57
     * @version 1.0
     */
    public class TaskThread implements Runnable {

        private String id;

        public TaskThread( String id) {
            this.id=id;
        }

        @Override
        public void run() {
            try {
                //根据传来的参数执行要定时的任务
                System.out.println("task id:"+id+",perform once task");

                // 获取现在的时间入参
                LocalDate date = LocalDate.now();
                recoRecordController.taskDeleteFastImage(date.toString());
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}
