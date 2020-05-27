package com.jitu.timeddeleted.tool;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 文件操作相关工具类
 * @author wangronglian
 * @date 2019年12月24日09:34:50
 * @version 1.0
 */
public class FileTool {

    /**根据byte[] 数组生成文件  （在本地）
     * @param bfile  字节数组
     * @param filePath 文件路径
     * @param fileName 文件名
     */
    public static void gengrateFile(byte[] bfile, String filePath,String fileName,String tableName,String date) {
        BufferedOutputStream bos = null;  //带缓冲得文件输出流
        FileOutputStream fos = null;      //文件输出流
        File file = null;
        try {
            filePath = filePath+"/"+tableName+"/"+date;
            System.out.println("filepath:"+filePath);
            File dir = new File(filePath);
            if(!dir.exists()){//判断文件目录是否存在
                dir.mkdirs();
            }
            file = new File(filePath+"/"+fileName);  //文件路径+文件名
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
