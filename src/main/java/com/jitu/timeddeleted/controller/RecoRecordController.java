package com.jitu.timeddeleted.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.tobato.fastdfs.exception.FdfsUnavailableException;
import com.jitu.timeddeleted.service.RecoRecordService;
import com.jitu.timeddeleted.tool.FastDfsClient;
import com.jitu.timeddeleted.tool.FileTool;
import com.jitu.timeddeleted.tool.OverMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 识别记录的controller层
 * @author wangronglian
 * @date 2019-10-12 17:29:32
 * @version 1.0
 */
@Controller
public class RecoRecordController {
    @Autowired
    RecoRecordService recoRecordService;

    @Autowired
    FastDfsClient fastDfsClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${deleteDays}")
    private Integer deleteDays;

    @Value("${delet.once.count}")
    private Integer deleteOnce;

    /**
     * 要删除的表名
     */
    @Value("${table.name}")
    private String tableName;

    /**
     * 要删除的表中对应的时间字段
     */
    @Value("${table.dateFile}")
    private String dateFile;

    /**
     * 要删除的表中对应的id字段
     */
    @Value("${table.id}")
    private String tableIds;

    /**
     * 要删除图片对应的字段
     */
    @Value("${table.filed}")
    private String filed;

    /**
     * 要删除图片对应的字段
     */
    @Value("${table.reco.delete}")
    private Boolean isDelatea;


    /**
     * 图片下载保存的路径
     */
    @Value("${download.filepath}")
    private String filePath;

    /**
     * 日志对象
     **/
    private static final Logger logger = LoggerFactory.getLogger(RecoRecordController.class);


    /**
     * 根据指定时间，删除该时间段内的图片
     * @param startTime 删除开始时间
     * @return 删除的数据
     */
    @RequestMapping("deleteFastImage")
    @ResponseBody
    public Object deleteFastImage(@RequestParam(value = "startTime" ,required = true)String startTime,
                                  @RequestParam(value = "endTime" ,required = true)String endTime){
        try {
            //修改map状态
            OverMap.currentMap.put("state",true);

            String[] tableNameSplit = tableName.split(";");
            String[] dateFileSplit = dateFile.split(";");
            String[] tableIdSplit= tableIds.split(";");


            // 遍历表，可能是多张表进行删除
            for (int k =0;k<tableNameSplit.length;k++){
                // 封装参数
                Map<String,Object> param = new HashMap<>();
                param.put("startTime",startTime);
                param.put("endTime",endTime);
                param.put("tableName",tableNameSplit[k]);
                param.put("dateFiled",dateFileSplit[k]);
                // 获取总记录条数
                Integer count = recoRecordService.selectRecoCount(param);
                Integer limit = deleteOnce;
                // 计算要执行的次数,并且遍历这些次数去删除
                Integer totalCount = (int)Math.ceil((double)count / (double)limit);
                for(int m = 0 ; m < totalCount ;m++){
                    Integer start = m*limit;
                    deleteFastImageByTable(tableNameSplit[k],startTime,endTime,dateFileSplit[k],tableIdSplit[k],start,limit);
                    logger.info("deleted "+(m+1)+"/"+totalCount+" complete,then start next deletion");
                    Thread.sleep(1000);
                }
                if(1 == tableNameSplit.length){
                    logger.info("Delete table: "+tableNameSplit[k]+" complete!");
                }else if(tableNameSplit.length !=1 && tableNameSplit.length==k+1){
                    logger.info("Delete "+(k+1)+"/"+tableNameSplit.length+" table: "+tableNameSplit[k]+" complete!");
                }else{
                    logger.info("Delete "+(k+1)+"/"+tableNameSplit.length+" table: "+tableNameSplit[k]+" complete! about to delete next table");
                }
                if(k<tableNameSplit.length){
                    logger.info("delete once complete sleep 10s");
                    Thread.sleep(10*1000);
                }
                // new Thread(new DeleteThread(tableNameSplit[k],startTime,endTime,dateFileSplit[k],tableIdSplit[k])).start();
            }

            //修改map状态
            OverMap.currentMap.put("state",false);
            return  "success";
        }catch (Exception e){
            e.printStackTrace();
            return "failed";
        }
    }

    /**
     * 下载图片，从fast接口部分，
     * @param startTime 开始时间，时间精确到分钟
     * @param endTime 结束时间
     * @return
     */
    @RequestMapping("downloadFastImg")
    @ResponseBody
    public String downloadFastImg(@RequestParam(value = "startTime" ,required = false)String startTime,
                                  @RequestParam(value = "endTime" ,required = false)String endTime){
        try {
            String[] tableNameSplit = tableName.split(";");
            String[] dateFileSplit = dateFile.split(";");

            for (int k =0;k<tableNameSplit.length;k++){
                downLoadImg(tableNameSplit[k],startTime,endTime,dateFileSplit[k]);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return  null;
    }

    /**
     * 遍历查询下载
     * @param tableName 表名
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param dateFiled 时间字段
     */
    private String downLoadImg(String tableName, String startTime, String endTime, String dateFiled) {
        Map<String,Object> param = new HashMap<>();
        // 时间字段的校验在service层实现
        param.put("startTime",startTime);
        param.put("endTime",endTime);
        param.put("dateFiled",dateFiled);
        param.put("tableName",tableName);
        List<Map<String, Object>> results = recoRecordService.downloadFastImg(param);

        // 解析字段
        String[] splitFiled = filed.split(";");

        if(results == null){
            return "noData";
        }else{
            Boolean flag = false;
            // 遍历，解析数据，进行下载
            Map<String,Object> tempMap = new HashMap<>();
            for(int i =0 ;i<results.size();i++){
                for(String key : results.get(i).keySet()){
                    if(flag){
                        flag = false;
                        break;
                    }
                    // 获取保存的字段
                    tempMap = results.get(i);
                    for(int m = 0 ;  m<splitFiled.length;m++){
                        String imageFiled = (String) tempMap.get(splitFiled[m]);
                        String currentDate = (String) tempMap.get(dateFiled);
                        if(imageFiled!=null){
                            downloadFile(imageFiled,tableName,currentDate);
                            flag = true;
                            break;
                        }
                    }
                }
            }
            return null;
        }
    }

    /**
     * 根据给定的图片路径和表明进行图片下载
     * @param url 图片的fast地址
     * @param tableName 表名（用来保存的文件夹名称）
     * @param currentDate 时间
     */
    private void downloadFile(String url, String tableName,String currentDate) {
        try {
            // url可能是三个或者多个拼接的,使用分号分割
            String[] urls = url.split(";");
            for(int  i = 0 ; i<urls.length;i++){
                try {
                    // 生成文件名,UUID
                    String[] urlsSplit = urls[i].split("/");
                    String fileName = urlsSplit[urlsSplit.length-1];
                     byte[] download = fastDfsClient.download(urls[i]);
                     FileTool.gengrateFile(download,filePath,fileName,tableName,currentDate.split(" ")[0]);
                     logger.info("download "+urls[i]+" success");
                }catch (Exception e){
                     logger.info(urls[i]+" node isn't exist,can't download");
                }
            }
        }catch (Exception e){
            logger.info(url+" node isn't exist,can't download");
        }
    }

    /**
     * 定时任务调用的删除数据接口
     * @param startTime
     * @return
     */
    public Object taskDeleteFastImage(String startTime){
        try {
            String lastDeleteDate=null;
            // 从redis获取上次的删除时间，如果没有设置现在的，有拿到库里的，并更新为传入的时间
            try {
                lastDeleteDate = redisTemplate.opsForValue().get("lastDeleteDate").toString();
                redisTemplate.opsForValue().set("lastDeleteDate",startTime);
            }catch (Exception e){
//                redisTemplate.opsForValue().set("lastDeleteDate",startTime);
            }
            if(lastDeleteDate == null){
                // 设置为当天日期
                // 如果从redis没拿到数据，则设置传入的时间为结束时间，当前时间为最后一次操作事件
                LocalDate date = LocalDate.now();

                lastDeleteDate = date.toString();

                // 保存这次的删除时间，作为下次删除的开始时间
                redisTemplate.opsForValue().set("lastDeleteDate",lastDeleteDate);

                // 定时任务第一次执行，设置传入时间（当前系统时间）为结束时间，开始时间为配置的删除天数之前的日期
                startTime = LocalDate.now().minusDays(deleteDays).toString();
            }

            String startTime1 = getStartTime(startTime, lastDeleteDate);
            String endTime1 = getEndTime(startTime, lastDeleteDate);

            String[] tableNameSplit = tableName.split(";");
            String[] dateFileSplit = dateFile.split(";");
            String[] tableIdSplit= tableIds.split(";");

            for (int k =0;k<tableNameSplit.length;k++){
                // 封装参数
                Map<String,Object> param = new HashMap<>();
                param.put("startTime",startTime);
                param.put("tableName",tableNameSplit[k]);
                param.put("dateFiled",dateFileSplit[k]);
                param.put("endTime",endTime1);
                Integer count = recoRecordService.selectRecoCount(param);
                Integer limit = deleteOnce;
                // 计算要执行的次数,并且遍历这些次数去删除，使用分页的方式进行删除，否则一次数据太多会导致线程出问题
                Integer totalCount = (int)Math.ceil((double)count / (double)limit);
                for(int m = 0 ; m < totalCount ;m++){
                    Integer start = m*limit;
                    deltetEveryTable(tableNameSplit[k],startTime1,endTime1,dateFileSplit[k],tableIdSplit[k],start,limit);
                }
            }
            return  "success";
        }catch (Exception e){
            e.printStackTrace();
            return "failed";
        }
    }

    /**
     * 删除数据部分，该部分逻辑只做删除数据-，将之前的时间处理部分分离出来
     * 页面接口调用和定时任务的时间处理在其他对应接口进行处理
     * @param tableItemName 表名
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param dateFiled 时间字段
     * @param tableId 表id
     * @param start 开始
     * @param limit 结束
     */
    private void deleteFastImageByTable(String tableItemName,String startTime,String endTime,String dateFiled,String tableId,Integer start,Integer limit){
        try {
            // 处理日期时间，添加时间后缀
            startTime = startTime+" 00:00:00";
            endTime = endTime+" 23:59:59";
            // 调整开始和结束时间，防止时间顺序错误

            startTime = "'" + startTime + "'";
            endTime = "'" + endTime + "'";

            // 解析字段
            String[] splitFiled = filed.split(";");

            // 配置参数
            Map<String,Object> param = new HashMap<>();
            param.put("tableName",tableItemName);
            param.put("startTime",startTime);// 开始时间和上次记录时间，刚好相反，封参调整
            param.put("endTime",endTime);
            param.put("dateFiled",dateFiled);
            param.put("tableId",tableId);
            // 分页查询参数
            param.put("start",start);
            param.put("limit",limit);
            List<Map<String,Object>> result = recoRecordService.selectByStartTime(param);

            String jsonString = JSON.toJSONString(result);
            JSONArray jsonArray = JSON.parseArray(jsonString);
            for (int i=0; i<jsonArray.size(); i++) {
                String node = "";
                try {
                    // 解析返回值，获取对应存放fast地址的字段
                    Object o = jsonArray.get(i);
                    String jsonItem = JSON.toJSONString(o);
                    JSONObject jsonObject = JSON.parseObject(jsonItem);

                    // 获取主键id对应的值，用来做删除部分,并将值传递到参数列表里面
                    String id = jsonObject.getString(tableId);
                    param.put("idValue",id);

                    // 根据字段解析结果
                    for(int m = 0 ; m < splitFiled.length;m++){
                        // 拿到要删除数据的节点，然后删除
                        String file_addr = jsonObject.getString(splitFiled[m]);
                        node  = file_addr;
                        if(node == null){
                            continue;
                        }
                        String[] nodeSplit = node.split(";");
                        for(int z = 0 ; z < nodeSplit.length; z++){
                            try {
                                String s = fastDfsClient.deleteFile2(nodeSplit[z]);
                                logger.info("delete "+ nodeSplit[z] +"result : "+s);
                            }catch (FdfsUnavailableException fe){
                                logger.info("delete failed ,无法获取服务端连接资源：找不到可用的tracker");
                                fe.printStackTrace();
                            }catch (Exception e){
                                logger.info(nodeSplit[z]+" node isn't exist,can't delete");
                            }
                        }
                    }

                    // 做删除处理
                    if(isDelatea){
                        //删除
                        Integer deleteResult = recoRecordService.deleteReco(param);
                        if(deleteResult== 1){
                            logger.info("sql reco delete success!");
                        }else{
                            logger.info("sql reco delete fail!");
                        }
                    }
                }catch (Exception e){
                    logger.info("node："+node+",doesn't exist，delete failed,exit delete;"+new Date().toString());
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 删除数据
     * @param tableItemName 表名
     * @param startTime 开始时间
     * @param lastDeleteTime 结束时间
     * @param dateFiled 时间字段
     * @param tableId 表id
     * @param start 开始
     * @param limit 结束
     */
    private void deltetEveryTable(String tableItemName,String startTime,String lastDeleteTime,String dateFiled,String tableId,Integer start,Integer limit){
        try {
            // 处理日期时间，添加时间后缀
            if(startTime == null){
                // 如果未传入时间，删除15天前的数据
                LocalDate date = LocalDate.now();

                date = date.minusDays(deleteDays);

                startTime = date.toString();
            }
            if(lastDeleteTime != null){
                lastDeleteTime = lastDeleteTime+" 23:59:59";
            }
            startTime = startTime+" 00:00:00";
            String endTime = lastDeleteTime;
            // 调整开始和结束时间，防止时间顺序错误

            startTime = "'" + startTime + "'";
            endTime = "'" + endTime + "'";

            // 解析字段
            String[] splitFiled = filed.split(";");

            // 配置参数
            Map<String,Object> param = new HashMap<>();
            param.put("tableName",tableItemName);
            param.put("startTime",startTime);// 开始时间和上次记录时间，刚好相反，封参调整
            param.put("endTime",endTime);
            param.put("dateFiled",dateFiled);
            param.put("tableId",tableId);
            // 分页查询参数
            param.put("start",start);
            param.put("limit",limit);
            List<Map<String,Object>> result = recoRecordService.selectByStartTime(param);

            String jsonString = JSON.toJSONString(result);
            JSONArray jsonArray = JSON.parseArray(jsonString);
            for (int i=0; i<jsonArray.size(); i++) {
                String node = "";
                try {
                    // 解析返回值，获取对应存放fast地址的字段
                    Object o = jsonArray.get(i);
                    String jsonItem = JSON.toJSONString(o);
                    JSONObject jsonObject = JSON.parseObject(jsonItem);

                    // 获取主键id对应的值，用来做删除部分,并将值传递到参数列表里面
                    String id = jsonObject.getString(tableId);
                    param.put("idValue",id);

                    // 根据字段解析结果
                    for(int m = 0 ; m < splitFiled.length;m++){
                        // 拿到要删除数据的节点，然后删除
                        String file_addr = jsonObject.getString(splitFiled[m]);
                        node  = file_addr;
                        if(node == null){
                            continue;
                        }
                        String[] nodeSplit = node.split(";");
                        for(int z = 0 ; z < nodeSplit.length; z++){
                            try {
                                String s = fastDfsClient.deleteFile2(nodeSplit[z]);
                                logger.info("delete "+ nodeSplit[z] +"result : "+s);
                            }catch (FdfsUnavailableException fe){
                                logger.info("delete failed ,无法获取服务端连接资源：找不到可用的tracker");
                                fe.printStackTrace();
                            }catch (Exception e){
                                logger.info(nodeSplit[z]+" node isn't exist,can't delete");
                            }
                        }
                    }

                    // 做删除处理
                    if(isDelatea){
                        //删除
                        Integer deleteResult = recoRecordService.deleteReco(param);
                        if(deleteResult== 1){
                            logger.info("sql reco delete success!");
                        }else{
                            logger.info("sql reco delete fail!");
                        }
                    }
                }catch (Exception e){
                    logger.info("node："+node+",doesn't exist，delete failed,exit delete;"+new Date().toString());
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String getEndTime(String startTime, String endTime)throws Exception {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date start = simpleDateFormat.parse(startTime);
        Date end = simpleDateFormat.parse(endTime);
        if(start.before(end)){
            return endTime;
        }else{
            return startTime;
        }
    }

    private String getStartTime(String startTime, String endTime)throws Exception{
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date start = simpleDateFormat.parse(startTime);
        Date end = simpleDateFormat.parse(endTime);
        if(start.before(end)){
            return startTime;
        }else{
            return endTime;
        }
    }

    /**
     * 轮询获取删除状态的接口
     * @return
     */
    @RequestMapping("getDeleteState")
    @ResponseBody
    private Boolean getDeleteState(){
        return (Boolean)OverMap.currentMap.get("state");
    }
}
