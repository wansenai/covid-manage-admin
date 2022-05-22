package com.summer.manage.dto;

/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/30 2:49 下午
 **/
public class GenConfig {

    /**
     * 作者
     */
    public static String author = "Sacher";

    /**
     * 生成包路径
     */
    public static String packageName = "com.thinker.admin";

    /**
     * 自动去除表前缀，默认是false
     */
    public static boolean autoRemovePre = false;

    /**
     * 表前缀(类名不会包含表前缀)
     */
    public static String tablePrefix;
}
