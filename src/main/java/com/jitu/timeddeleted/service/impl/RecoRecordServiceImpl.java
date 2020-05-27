package com.jitu.timeddeleted.service.impl;

import com.jitu.timeddeleted.dao.RecoRecordDao;
import com.jitu.timeddeleted.service.RecoRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * recoRecord service层的实现类
 * @author wangronglian
 * @date 2019-10-12 17:34:55
 * @version 1.0
 */
@Service
public class RecoRecordServiceImpl implements RecoRecordService {

    @Autowired
    RecoRecordDao recoRecordDao;

    @Override
    public List<Map<String,Object>> selectByStartTime(Map<String, Object> params) {
        return recoRecordDao.selectByStartTime(params);
    }

    @Override
    public Integer deleteReco(Map<String, Object> param) {
       return recoRecordDao.deleteReco(param);
    }

    @Override
    public List<Map<String,Object>> downloadFastImg(Map<String, Object> param) {
        String startTime = (String) param.get("startTime");
        String endTime = (String) param.get("endTime");
        startTime = startTime.replace("T"," ");
        endTime = endTime.replace("T"," ");

        param.put("startTime","'"+startTime+":00'");
        param.put("endTime","'"+endTime+":00'");
        List<Map<String, Object>> maps = recoRecordDao.selectByStartTime(param);
        return  maps;
    }

    @Override
    public Integer selectRecoCount(Map<String, Object> param) {
        // 处理时间格式
        String startTime = (String) param.get("startTime");
        String endTime = (String) param.get("endTime");

        param.put("startTime","'"+startTime+" 00:00:00'");
        param.put("endTime","'"+endTime+" 23:59:59'");
        return recoRecordDao.selectRecoCount(param);
    }
}
