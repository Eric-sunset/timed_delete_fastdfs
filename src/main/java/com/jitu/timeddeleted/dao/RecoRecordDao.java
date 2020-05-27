package com.jitu.timeddeleted.dao;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * 查询记录的dao
 * @author  wangronglian
 * @date 2019-10-12 17:02:30
 * @version 1.0
 */
@Mapper
public interface RecoRecordDao {

    List<Map<String,Object>> selectByStartTime(Map<String, Object> params) ;

    Integer deleteReco(Map<String, Object> param);

    Integer selectRecoCount(Map<String, Object> param);
}
