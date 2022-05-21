package com.summer.common.helper;

import org.springframework.aop.framework.AopContext;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.util.function.Supplier;

/** SPRING容器工具类 **/
public abstract class SpringHelper {
    private static ApplicationContext ctx; private static final String EMPTY = "";

    public void setApplicationContext(ApplicationContext context) {
        SpringHelper.ctx = context;
    }

    public static Environment getEnvironment() {
        return null == ctx ? null : ctx.getEnvironment();
    }

    public static <T> T getBean(Class<T> clazz) {
        return ctx.getBean(clazz);
    }

    public static <T> T getBean(String beanName, Class<T> clazz) {
        return ctx.getBean(beanName, clazz);
    }


    @SuppressWarnings("unused")
    private static String CONTEXT_PATH, APP_NAME, APP_PORT;

    /** context path of the application. **/
    public static String contextPath() {
        return ofValue(CONTEXT_PATH, ()-> confValue("server.servlet.context-path"));
    }

    /** 获取应用名 **/
    public static String applicationName() {
        return ofValue(APP_NAME, ()-> confValue("spring.application.name"));
    }

    /** 获取应用端口 **/
    public static String applicationPort() {
        String port = ofValue(APP_PORT, ()-> confValue("server.port"));
        return (null == port || port.trim().length() < 1) ? "0" : port;
    }

    /** msc.response.used **/
    public static boolean mscResponseUsed() {
        return Boolean.TRUE.toString().equals(confValue("msc.response.used"));
    }

    /** 获取应用运行的环境 **/
    public static String applicationEnv() {
        if(null == ctx || null == ctx.getEnvironment()) {
            return EMPTY;
        }
        String[] envList = ctx.getEnvironment().getActiveProfiles();
        if (null != envList && envList.length > 0) {
            return envList[0];
        } else {
            String conf = ctx.getEnvironment().getProperty("boot.active-conf");
            if (null != conf && conf.length() > 0) {
                return conf;
            }
        }
        return EMPTY;
    }

    /** 获取应用环境KEY值 **/
    public static String confValue(String key) {
        if(null == ctx || null == ctx.getEnvironment()) {
            return EMPTY;
        }
        String value = ctx.getEnvironment().getProperty(key);
        return null == value ? EMPTY : value.trim();
    }

    private static String ofValue(String rs, Supplier<String> supplier) {
        if(null == rs || rs.trim().length() < 1) {
            rs = supplier.get();
        }
        return rs;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getAopProxy(T invoker) {
        return (T) AopContext.currentProxy();
    }
}
