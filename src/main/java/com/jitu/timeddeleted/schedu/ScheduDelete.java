/*
package com.jitu.timeddeleted.schedu;

import com.jitu.timeddeleted.controller.RecoRecordController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;

@Component
@EnableScheduling
public class ScheduDelete {
    @Autowired
    RecoRecordController recoRecordController;

    @Value("${deleteDays}")
    private Integer deleteDays;

    @Value("${corn.value}")
    private Integer corn;

    //  每个月的22号，16点 19/20/21/22/23/24分进行任务执行
//    @Scheduled(cron = "* 00 00 1,15 * ?")
//    @Scheduled(cron = "0 00 00 1,15 * ?")
    @Scheduled(cron = "0 00 00 1,15 * ?")
    public void task(){
        System.out.println("定时任务,"+new Date().toString());
        // 删除当前日期前十五天的fast数据
        LocalDate date = LocalDate.now();

        date = date.minusDays(deleteDays);
        System.out.println(recoRecordController.deleteFastImage(date.toString()));
    }
}
*/
