package com.summer.common.helper;

import com.google.common.collect.Maps;
import javafx.util.Pair;
import org.springframework.util.AntPathMatcher;

import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则匹配工具类
 **/
public final class MatcherHelper {
    public static final Pattern EMAIL = Pattern.compile("^[a-zA-Z0-9._\\-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");
    public static final Pattern BASE64 = Pattern.compile("^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$");

    private static final ConcurrentMap<String, Pattern> REGULAR_P = Maps.newConcurrentMap();
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    private MatcherHelper() {
    }

    /**
     * 是否邮箱校验
     **/
    public static boolean isEmail(String email) {
        return EMAIL.matcher(email).find();
    }

    /**
     * 是否IPV4校验
     **/
    public static boolean isIpAddress(String ip) {
        String[] p = ip.split("\\.");
        if (p.length != 4) return false;
        for (String pp : p) {
            if (pp.length() > 3) return false;
            int val = Integer.valueOf(pp);
            if (val > 255) return false;
        }
        return true;
    }

    /**
     * 取出匹配到的字符患起止位置及值
     **/
    public static LinkedHashMap<Pair<Integer, Integer>, String> matches(String src, String regular) {
        LinkedHashMap<Pair<Integer, Integer>, String> matches = Maps.newLinkedHashMap();
        if (StringHelper.isBlank(regular) || StringHelper.isBlank(src)) {
            return matches;
        }
        Pattern pattern = REGULAR_P.get(regular);
        if (null == pattern) {
            pattern = Pattern.compile(regular);
            REGULAR_P.put(regular, pattern);
        }
        Matcher matcher = pattern.matcher(src);
        while (matcher.find()) {
            matches.put(new Pair<>(matcher.start(), matcher.end()), matcher.group());
        }
        return matches;
    }

    /**
     * URI匹配
     **/
    public static void hitAntPathMatcherCache(final Set<String> uriSet) {
        if (!CollectsHelper.isNullOrEmpty(uriSet)) {
            pathMatcher.setCachePatterns(true);
            for (String pattern : uriSet) {
                // hit on cache
                pathMatcher.match(pattern, StringHelper.EMPTY);
            }
        }
    }

    /**
     * URI路径匹配
     **/
    public static String matchPath(String uri, final Set<String> uriSet) {
        for (String pattern : uriSet) {
            if (pathMatcher.match(pattern, uri)) {
                return pattern;
            }
        }
        return "";
    }

    /**
     * 是否符合BASE64字符串
     **/
    public static boolean isBase64(String src) {
        if (StringHelper.isBlank(src)) {
            return false;
        } else {
            if (src.length() % 4 != 0) {
                return false;
            }
            return BASE64.matcher(src).find();
        }
    }
}
