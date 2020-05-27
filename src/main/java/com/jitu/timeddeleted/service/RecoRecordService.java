package com.jitu.timeddeleted.service;

import java.util.List;
import java.util.Map;

/**
 * 识别记录的service接口
 * @author wangronglian
 * @date 2019-10-12 17:26:44
 * @version 1.0
 */
public interface RecoRecordService {

    List<Map<String,Object>> selectByStartTime(Map<String, Object> params);

    Integer deleteReco(Map<String, Object> param);

    List<Map<String,Object>> downloadFastImg(Map<String, Object> param);

    Integer selectRecoCount(Map<String, Object> param);
}
