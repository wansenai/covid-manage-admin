package com.summer.common.helper;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/** 字符串工具类 **/
public final class StringHelper {
    private static final Pattern pattern = Pattern.compile("^-?\\d+(\\.\\d+)?$");
    public static String EMPTY = "";

    private static final int INDEX_NOT_FOUND = -1;
    private StringHelper() {
    }

    public static String toString(final Object src) {
        return src == null ? EMPTY : src.toString();
    }

    public static String defaultString(final String src) {
        return isBlank(src) ? EMPTY : src;
    }

    public static String defaultIfBlank(final String src, final String defaultVal) {
        return isBlank(src) ? defaultVal : src;
    }

    public static String trimString(final String src) {
        return isBlank(src) ? EMPTY : src.trim();
    }

    public static String trimIfBlank(final String src, final String defaultVal) {
        return isBlank(src) ? defaultVal : src.trim();
    }

    public static boolean isBlank(final String src) {
        int strLen;
        if (src == null || (strLen = src.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(src.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static String leftPad(final String src, final int size, char pad) {
        String lpS = defaultString(src);
        int lpLen = lpS.length();
        if (lpLen == size) {
            return lpS;
        }
        if (lpS.length() > size) {
            return lpS.substring(lpS.length() - size);
        }
        int padLen = size - src.toCharArray().length;
        char[] chars = new char[padLen];
        for (int i = 0; i < padLen; i++) {
            chars[i] = pad;
        }
        return new String(chars) + src;
    }

    public static int length(final CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

    public static String substringBetween(final String src, final String open, final String close) {
        if (src == null || open == null || close == null) {
            return null;
        }
        final int start = src.indexOf(open);
        if (start != INDEX_NOT_FOUND) {
            final int end = src.indexOf(close, start + open.length());
            if (end != INDEX_NOT_FOUND) {
                return src.substring(start + open.length(), end);
            }
        }
        return EMPTY;
    }

    public static boolean isNumeric(String src) {
        return !isBlank(src) && pattern.matcher(src).matches();
    }

    public static List<String> list(String splitter, String src) {
        if(EMPTY.equals(splitter) && !isBlank(src)) {
            return Lists.newArrayList(src.split(splitter));
        }
        return Splitter.on(splitter).splitToList(defaultString(src));
    }

    public static Map<String, String> map(String src, String splitter, String separator) {
        if(isBlank(src) || isBlank(splitter) || isBlank(separator)){
            return Maps.newHashMap();
        }
        return Splitter.on(splitter).withKeyValueSeparator(separator).split(src);
    }

    public static <T> String join(String splitter, T ...src) {
        if(CollectsHelper.isNullOrEmpty(src)) {
            return EMPTY;
        }
        List<T> list = Lists.newArrayList();
        for (T t: src) {
            if (t instanceof Collection) {
                list.addAll((Collection)t);
            } else {
                list.add(t);
            }
        }
        return Joiner.on(splitter).join(list);
    }

    @Deprecated
    public static <T> String join(Collection<T> src, String splitter) {
        if(CollectsHelper.isNullOrEmpty(src)) {
            return EMPTY;
        }
        return Joiner.on(splitter).join(src);
    }

    /** 下划线转驼峰 **/
    public static String underline2camel(String src) {
        if (isBlank(src)) {
            return StringHelper.EMPTY;
        }
        if(src.contains("_")) {
            StringTokenizer tokenizer = new StringTokenizer(src, "_");
            StringBuilder sb = new StringBuilder();
            boolean firstTime = true;
            while (tokenizer.hasMoreTokens()) {
                String word = tokenizer.nextToken();
                if (firstTime) {
                    sb.append(word.toLowerCase());
                    firstTime = false;
                } else {
                    String lowercase = word.toLowerCase();
                    sb.append(lowercase.substring(0, 1).toUpperCase());
                    sb.append(lowercase.substring(1));
                }
            }
            return sb.toString();
        } else {
            return src;
        }
    }
    /** 驼峰转下划线 **/
    public static String camel2Underline(String src){
        if (isBlank(src)) {
            return StringHelper.EMPTY;
        }
        int len = src.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = src.charAt(i);
            if ('"' == c) {
                continue;
            }
            if (Character.isUpperCase(c)){
                sb.append("_").append(Character.toLowerCase(c));
            }else{
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /** 集合转换成IBatis中的IN参数 **/
    public static <T extends Serializable> String toIbatisIn(Collection<T> ids) {
        if (!CollectsHelper.isNullOrEmpty(ids)) {
            StringBuilder idsBuilder = new StringBuilder();
            // 过滤重复的ID
            LinkedHashSet<T> idSet = Sets.newLinkedHashSet(ids);
            for (T serial : idSet) {
                // 数字类型直接逗号分隔，字符串每个逗分隔都要带单引号
                if (serial instanceof Number) {
                    idsBuilder.append(",").append(serial);
                } else {
                    if (null != serial && !isBlank(serial.toString())) {
                        idsBuilder.append(",'").append(serial).append("'");
                    }
                }
            }
            if (idsBuilder.length() > 0) {
                return idsBuilder.replace(0, 1, "(").append(")").toString();
            }
        }
        return EMPTY;
    }

    /** 替换EMOJI表情符 **/
    public static String replaceEmoji(String source, String pad) {
        if (null == source || "".equals(source)) {
            return StringHelper.EMPTY;
        }
        int len = source.length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            char codePoint = source.charAt(i);
            sb.append(isEmoji(codePoint) ? pad : codePoint);
        }
        return sb.toString();
    }

    /** 判断字符串是否包含EMOJI表情 **/
    public static boolean containsEmoji(String source) {
        if (null == source || "".equals(source)) {
            return false;
        }
        int len = source.length();
        for (int i = 0; i < len; i++) {
            char codePoint = source.charAt(i);
            if (isEmoji(codePoint)) {
                return true;
            }
        }
        return false;
    }
    private static boolean isEmoji(char code) {
        // 杂项符号与符号字体
        return (code >= 0x2600 && code <= 0x27BF)
                || code == 0x303D
                || code == 0x2049
                || code == 0x203C
                || (code >= 0x2000 && code <= 0x200F)
                || (code >= 0x2028 && code <= 0x202F)
                || code == 0x205F
                || (code >= 0x2065 && code <= 0x206F)
                // 字符 �
                || code == 0xFFFD
                // 字母符号
                || (code >= 0x2100 && code <= 0x214F)
                // 各种技术符号
                || (code >= 0x2300 && code <= 0x23FF)
                // 箭头A
                || (code >= 0x2B00 && code <= 0x2BFF)
                // 箭头B
                || (code >= 0x2900 && code <= 0x297F)
                // 中文符号
                || (code >= 0x3200 && code <= 0x32FF)
                // 高低位替代符保留区域
                || (code >= 0xD800 && code <= 0xDFFF)
                // 私有保留区域
                || (code >= 0xE000 && code <= 0xF8FF)
                // 变异选择器
                || (code >= 0xFE00 && code <= 0xFE0F)
                // Plane在第二平面以上的，char都不可以存
                || code >= 0x10000;
    }
}
