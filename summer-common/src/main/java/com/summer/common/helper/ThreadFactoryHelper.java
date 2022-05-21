package com.summer.common.helper;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** 线程工具类 **/
public final class ThreadFactoryHelper {
    private static final Logger LOG = LoggerFactory.getLogger(ThreadFactoryHelper.class);
    private static final ExecutorService CAPTURES = Executors.newFixedThreadPool(16, threadFactoryOf("CAPTURES"));

    private static int MAX_THREADS = 32768, TIMEOUT = 15;

    private ThreadFactoryHelper() {
    }

    public static List<ResultSet> captures(Supplier<Object> ...suppliers) {
        if(null != suppliers && suppliers.length > 0) {
            return captures(Arrays.asList(suppliers), TIMEOUT);
        }
        return Lists.newArrayList();
    }

    public static List<ResultSet> captures(int seconds, Supplier<Object> ...suppliers) {
        if(null != suppliers && suppliers.length > 0) {
            return captures(Arrays.asList(suppliers), seconds);
        }
        return Lists.newArrayList();
    }

    public static List<ResultSet> captures(List<Supplier<Object>> suppliers) {
        return captures(suppliers, TIMEOUT);
    }
    public static List<ResultSet> captures(List<Supplier<Object>> suppliers, int seconds) {
        if(null != suppliers || suppliers.size() > 0) {
            final List<Future<ResultSet>> futures = suppliers.stream().map(s -> CAPTURES.submit(() -> {
                try {
                    if (null != s) {
                        return new ResultSet(true, s.get());
                    } else {
                        return new ResultSet(false, new NullPointerException("action supplier is null"));
                    }
                } catch (Exception e) {
                    return new ResultSet(false, e);
                }
            })).collect(Collectors.toList());
            List<ResultSet> captures = Lists.newArrayList();
            for (Future<ResultSet> future : futures) {
                try {
                    captures.add(future.get(seconds < 1 ? TIMEOUT : seconds, TimeUnit.SECONDS));
                } catch (Exception e) {
                    captures.add(new ResultSet(false, e));
                } finally {
                    if(null != future){
                        future.cancel(true);
                    }
                }
            }
            return captures;
        }
        return Lists.newArrayList();
    }

    /** 创建带缓存的线程池 **/
    public static ExecutorService newCachedThreadPool(String name) {
        return new ThreadPoolExecutor(0, MAX_THREADS, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), threadFactoryOf(name));
    }

    /** 设置线程名称 **/
    public static ThreadFactory threadFactoryOf(String name) {
        String nameFormat = StringHelper.defaultIfBlank(name, "ES") + "@t%d";
        Thread.UncaughtExceptionHandler eh = (t, e) -> LOG.warn("Thread {} has unexpected error ", t.getName(), e);
        return new ThreadFactoryBuilder().setNameFormat(nameFormat).setUncaughtExceptionHandler(eh).build();
    }

    public static class ResultSet {
        public final boolean result;

        public final Object data;

        public final Throwable cause;

        private ResultSet(boolean result, Object data) {
            this.result = result;
            this.data = result ? data : null;
            this.cause = !result ? (Throwable) data: null;
        }
    }
}
