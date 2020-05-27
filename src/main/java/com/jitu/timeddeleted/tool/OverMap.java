package com.jitu.timeddeleted.tool;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局map，用来保存删除fast图片的状态
 * false未删除，不在删除中，true,删除中
 * @author wangronglian
 * @date 2020年4月26日14:03:04
 * @version 1.0
 */
public class OverMap {


    public final static Map currentMap = new HashMap();


    static {
        currentMap.put("state",false);
    }
}
