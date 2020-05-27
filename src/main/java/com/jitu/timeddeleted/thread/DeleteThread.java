package com.jitu.timeddeleted.thread;

/**
 * 删除fast图片的线程任务，防止多删除的情况下，内存溢出等问题,功能未实现。
 * @author wangronglian
 * @date 2020年1月6日15:08:05
 * @version 1.9
 */
public class DeleteThread implements Runnable{
    private String tableName;

    private  String startTime;

    private String endTime;

    private String dateFiled;

    private String tableId;

    public DeleteThread(String tableName,String startTime,String endTime,String dateFiled,String tableId){
        this.tableName=tableName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.dateFiled= dateFiled;
        this.tableId = tableId;
    }

    @Override
    public void run() {
        System.out.println(tableName);
        System.out.println(startTime);
        System.out.println(endTime);
        System.out.println(dateFiled);
        System.out.println(tableId);
    }
}
