package com.summer.common.initializ;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.summer.common.helper.MatcherHelper;
import com.summer.common.helper.SpringHelper;
import com.summer.common.support.IConstant;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public abstract class SpringBootApplication implements CommandLineRunner {
    private static final Map<Integer, ListMultimap<String, Pair<Long, Long>>> RECORDS = new ConcurrentHashMap<>();
    protected static volatile Logger LOG;
    // 最大统计窗口时间，1s = 1000ms
    private static long RECKS_WINDOW = 5L, SECOND = 1000L, SCALE = 2L;
    private static final Cache<Integer, Integer> RING = CacheBuilder.newBuilder()
                                                                    .maximumSize(RECKS_WINDOW)
                                                                    .expireAfterWrite(RECKS_WINDOW, TimeUnit.MINUTES)
                                                                    .removalListener(nfy -> {
                                                                        if (nfy.wasEvicted()) {
                                                                            RECORDS.get(nfy.getKey()).clear();
                                                                        }
                                                                    }).build();
    private static Set<String> URI_S = new HashSet<>(), IGNORE = Sets.newHashSet("", "/", "/rest", "/error", "/favicon.ico");

    static {
        for (int i = 0; i < 60; i++) {
            RECORDS.put(i, ArrayListMultimap.create());
        }
    }

    private final String APP;

    public SpringBootApplication() {
        Class<?> clz = this.getClass();
        this.APP = clz.getSimpleName();
        SpringBootApplication.LOG = LoggerFactory.getLogger(clz);
    }

    /**
     * 获取项目所有 URI
     **/
    public static Set<String> urisGet() {
        return Collections.unmodifiableSet(URI_S);
    }

    /**
     * 所有URI请求统计，最近5分钟以内的数据
     **/
    public static Map<String, Map<Long, Map<String, Number>>> requestStatsGet() {
        RING.getIfPresent(LocalDateTime.now().getMinute());
        waitForRingCalc();
        Map<Integer, ListMultimap<String, Pair<Long, Long>>> records = Collections.unmodifiableMap(RECORDS);
        Map<String, Multimap<Long, Long>> calc = new HashMap<>();
        for (String uri : URI_S) {
            calc.put(uri, ArrayListMultimap.create());
        }
        for (Map.Entry<Integer, ListMultimap<String, Pair<Long, Long>>> entry : records.entrySet()) {
            if (entry.getValue().size() > 0) {
                for (Map.Entry<String, Collection<Pair<Long, Long>>> data : entry.getValue().asMap().entrySet()) {
                    Multimap<Long, Long> dMap = calc.get(data.getKey());
                    for (Pair<Long, Long> pair : data.getValue()) {
                        dMap.put(pair.getKey() / SECOND, pair.getValue() - pair.getKey());
                    }
                }
            }
        }
        Map<String, Map<Long, Map<String, Number>>> stats = new HashMap<>();
        for (Map.Entry<String, Multimap<Long, Long>> entry : calc.entrySet()) {
            Map<Long, Map<String, Number>> dcm = new TreeMap<>();
            for (Map.Entry<Long, Collection<Long>> data : entry.getValue().asMap().entrySet()) {
                dcm.put(data.getKey(), calcStats(data.getValue()));
            }
            stats.put(entry.getKey(), dcm);
        }
        return stats;
    }

    /**
     * 实例请求统计，最近5分钟以内的数据
     **/
    public static Map<Long, Map<String, Number>> instStatsGet() {
        RING.getIfPresent(LocalDateTime.now().getMinute());
        waitForRingCalc();
        Map<Integer, ListMultimap<String, Pair<Long, Long>>> records = Collections.unmodifiableMap(RECORDS);
        Multimap<Long, Long> calc = ArrayListMultimap.create();
        for (Map.Entry<Integer, ListMultimap<String, Pair<Long, Long>>> entry : records.entrySet()) {
            if (entry.getValue().size() > 0) {
                for (Map.Entry<String, Collection<Pair<Long, Long>>> data : entry.getValue().asMap().entrySet()) {
                    for (Pair<Long, Long> pair : data.getValue()) {
                        calc.put(pair.getKey() / SECOND, pair.getValue() - pair.getKey());
                    }
                }
            }
        }
        Map<Long, Map<String, Number>> stats = new TreeMap<>();
        int counts = 0, times = 0;
        double qps = 0d;
        if (calc.asMap().size() > 0) {
            for (Map.Entry<Long, Collection<Long>> data : calc.asMap().entrySet()) {
                counts += data.getValue().size();
                times += 1;
                stats.put(data.getKey(), calcStats(data.getValue()));
            }
            qps = new BigDecimal(counts).divide(new BigDecimal(times), (int) SCALE, BigDecimal.ROUND_HALF_UP).doubleValue();
        }
        stats.put(0L, ImmutableMap.of("counts", counts, "qps", qps));
        return stats;
    }

    /**
     * spring boot 服务启动
     *
     * @param mainCLZ
     * @param args
     **/
    protected static ConfigurableApplicationContext start(Class mainCLZ, String[] args) {
        try {
            return SpringApplication.run(mainCLZ, args);
        } catch (Exception e) {
            LoggerFactory.getLogger(mainCLZ).info("{} startup failure then terminated ", mainCLZ.getSimpleName());
            System.exit(Integer.MAX_VALUE);
            return null;
        }
    }

    /**
     * 所有URI请求统计，只保留最近5分钟的数据
     **/
    public static void doStatisticsUriRequest(Long accessTime, String uri) {
        int ring = LocalDateTime.now().getMinute();
        RING.put(ring, ring);
        if (URI_S.contains(uri)) {
            RECORDS.get(ring).put(uri, new Pair<>(accessTime, System.currentTimeMillis()));
        } else {
            String pattern = MatcherHelper.matchPath(uri, URI_S);
            if (null != pattern && pattern.length() > 0) {
                RECORDS.get(ring).put(pattern, new Pair<>(accessTime, System.currentTimeMillis()));
            }
        }
    }

    private static Map<String, Number> calcStats(Collection<Long> values) {
        double size = values.size(), avg = 0.00, sum = 0;
        long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
        if (size > 0) {
            for (Long time : values) {
                sum += time;
                if (time < min) {
                    min = time;
                }
                if (time > max) {
                    max = time;
                }
            }
            avg = new BigDecimal(sum).divide(new BigDecimal(size), (int) SCALE, BigDecimal.ROUND_HALF_UP).doubleValue();
        }
        return ImmutableMap.of("max", (int) max, "min", (int) min, "avg", avg, "count", values.size());
    }

    private static void waitForRingCalc() {
        try {
            TimeUnit.MILLISECONDS.sleep(10L);
        } catch (Exception e) {
            LOG.warn("Wait for ring calc error ", e);
        }
    }

    @Override
    public final void run(String... args) {
        LOG.info("{} on at {} environment", APP, SpringHelper.applicationEnv());
        initializeRequestMappingS();
        // Logback Reset Initializer
        new LogbackResetInitializer().configure();
        LOG.debug("COUNT={} jobs run", jobs(args));
    }

    /**
     * 后台任务入口
     **/
    protected abstract int jobs(String... args);

    private void initializeRequestMappingS() {
        RequestMappingHandlerMapping mapping = SpringHelper.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        // 获取url与类和方法的对应信息
        Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();
        for (RequestMappingInfo info : map.keySet()) {
            // 获取url的Set集合，一个方法可能对应多个url
            Set<String> patterns = info.getPatternsCondition().getPatterns();
            for (String uri : patterns) {
                if (!IGNORE.contains(uri) && !IConstant.isActuatorEndpoint(uri)) {
                    URI_S.add(uri);
                }
            }
        }
    }
}
