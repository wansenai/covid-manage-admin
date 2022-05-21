package com.summer.common.helper;

import com.google.common.collect.ImmutableMap;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/** JVM信息工具类 **/
public final class JvmOSHelper {
    private static final Logger LOG = LoggerFactory.getLogger(JvmOSHelper.class);
    private static final BigDecimal SIZE_M = BigDecimal.valueOf(1024 * 1024L); private static final int SCALE = 2;
    private JvmOSHelper() {}
    @SuppressWarnings("unused")
    private static volatile String OS_NAME, OS_ARCH, FILE_SEPARATOR, PROJECT_DIR;
    /** 是否Window操作系统 **/
    public static boolean isWindows() {
        return ofValue(OS_NAME, ()-> System.getProperty("os.name")).toUpperCase().startsWith("WIN");
    }
    /** 获取操作系统版本 **/
    public static boolean isV64() {
        return ofValue(OS_ARCH, ()-> System.getProperty("os.arch")).endsWith("64");
    }

    /** JVM 堆内存，key=MAX、value=USED **/
    public static Map<String, Number> heapMemory() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemory = memoryMXBean.getHeapMemoryUsage();
        BigDecimal used = BigDecimal.valueOf(heapMemory.getUsed()).divide(SIZE_M, SCALE, BigDecimal.ROUND_HALF_UP);
        BigDecimal max = BigDecimal.valueOf(heapMemory.getCommitted()).divide(SIZE_M, SCALE, BigDecimal.ROUND_HALF_UP);
        return ImmutableMap.of("max", max, "used", used);
    }
    /** JVM 非堆内存，key=MAX、value=USED **/
    public static Map<String, Number> nonHeapMemory() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemory = memoryMXBean.getNonHeapMemoryUsage();
        BigDecimal used = BigDecimal.valueOf(heapMemory.getUsed()).divide(SIZE_M, SCALE, BigDecimal.ROUND_HALF_UP);
        BigDecimal max = BigDecimal.valueOf(heapMemory.getCommitted()).divide(SIZE_M, SCALE, BigDecimal.ROUND_HALF_UP);
        return ImmutableMap.of("max", max, "used", used);
    }
    /** JVM 直接内存，key=MAX、value=USED **/
    public static Map<String, Number> directMemory() {
        BigDecimal max = BigDecimal.valueOf(sun.misc.VM.maxDirectMemory()).divide(SIZE_M, SCALE, BigDecimal.ROUND_HALF_UP);
        try {
            Class<?> c = Class.forName("java.nio.Bits");
            Field reservedM = c.getDeclaredField("reservedMemory");
            boolean accessible = reservedM.isAccessible();
            reservedM.setAccessible(true);
            synchronized (reservedM) {
                long used = ((AtomicLong) reservedM.get(null)).get();
                reservedM.setAccessible(accessible);
                return ImmutableMap.of("max", max, "used", BigDecimal.valueOf(used).divide(SIZE_M, SCALE, BigDecimal.ROUND_HALF_UP));
            }
        } catch (Exception e) {
            LOG.error("Get used direct memory error ", e);
            return ImmutableMap.of("max", max, "used", BigDecimal.ZERO);
        }
    }
    /** 不同系统的文件分隔符 **/
    public static String fileSeparator() {
        return ofValue(FILE_SEPARATOR, () -> System.getProperty("file.separator"));
    }
    /** 获取工程所在目录 **/
    public static String projectDir() {
        return ofValue(PROJECT_DIR, ()-> System.getProperty("user.dir"));
    }
    /** 获取指定包下指定子类型的CLASS列表 **/
    public static <T> Set<Class<? extends T>> classesSubOf(String basePackage, final Class<T> typed) {
        Reflections reflections = new Reflections(basePackage);
        return reflections.getSubTypesOf(typed);
    }
    /** 获取指定包下指定注解的CLASS列表 **/
    public static Set<Class<?>> classesAnnotatedWith(String basePackage, final Class<? extends Annotation> annotated) {
        Reflections reflections = new Reflections(basePackage);
        return reflections.getTypesAnnotatedWith(annotated);
    }

    private static String ofValue(String rs, Supplier<String> supplier) {
        if(StringHelper.isBlank(rs)) {
            rs = supplier.get();
        }
        return rs;
    }
}
