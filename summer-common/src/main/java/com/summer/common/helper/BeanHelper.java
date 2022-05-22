package com.summer.common.helper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rits.cloning.Cloner;
import com.summer.common.core.StringEnum;
import com.summer.common.support.DateFormat;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * BEAN类工具
 **/
public final class BeanHelper {
    private static final Logger LOG = LoggerFactory.getLogger(BeanHelper.class);

    private static final ConcurrentMap<Class<?>, ConcurrentMap<String, Field>> cfMap = Maps.newConcurrentMap();
    private static ModelMapper modelMapper = new ModelMapper();
    private static Cloner cloner = new Cloner();

    static {
        modelMapper.getConfiguration()
                   .setFieldMatchingEnabled(true)                         // 字段匹配
                   .setFullTypeMatchingRequired(true)                     // 类型匹配
                   .setMatchingStrategy(MatchingStrategies.STRICT)        // 严格策略
                   .setPropertyCondition(ctx -> null != ctx.getSource())    // 过滤NULL
                   .setProvider(request -> {
                       if (null != request.getSource() && requireClone(request.getSource().getClass())) {
                           return cloner.deepClone(request.getSource());
                       }
                       return null;
                   });
    }

    private BeanHelper() {
    }

    /**
     * 对象拷贝
     **/
    public static void copy(final Object source, final Object target) {
        if (null != source && null != target) {
            modelMapper.map(source, target);
        }
    }

    /**
     * 对象克隆
     **/
    public static <Source, Target> Target castTo(final Source source, final Class<Target> destType) {
        if (null == source || null == destType) {
            return null;
        }
        try {
            Target target = destType.newInstance();
            copy(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("error to copy source as " + destType.getName(), e);
        }
    }

    /**
     * 集合对象克隆
     **/
    public static <Source, Target> List<Target> castTo(final Collection<Source> source, final Class<Target> targetType) {
        if (CollectsHelper.isNullOrEmpty(source)) {
            return Lists.newArrayListWithExpectedSize(0);
        }

        List<Target> result = Lists.newArrayListWithExpectedSize(source.size());
        for (Source src : source) {
            result.add(castTo(src, targetType));
        }
        return result;
    }

    /**
     * Map 转 Bean
     **/
    public static <T> T map2Bean(final Map<String, Object> source, final Class<T> clazz) {
        return JsonHelper.parseObject(source instanceof JSONObject ? source.toString() : new JSONObject(source).toString(), clazz);
    }

    /**
     * Bean 转 Map
     **/
    public static Map<String, Object> bean2Map(Object source) {
        return source instanceof JSONObject ? (JSONObject) source : (JSONObject) JSON.toJSON(source);
    }

    /**
     * 对象转 MultiValueMap
     **/
    public static MultiValueMap<String, Object> toMultiMap(final Object source) {
        MultiValueMap<String, Object> mvm = new LinkedMultiValueMap<>();
        mvm.setAll(bean2Map(source));
        return mvm;
    }

    /**
     * 设置对象属性值
     **/
    public static void setProperty(final Object bean, final String name, final Object value) {
        Field field = FieldUtils.getField(bean.getClass(), name, true);
        try {
            if (null == field || null == value) {
                return;
            }
            Class<?> fClazz = field.getType();
            Class<?> vClazz = value.getClass();
            if ((StringEnum.class.isAssignableFrom(fClazz) || Enum.class.isAssignableFrom(fClazz)) && vClazz.equals(String.class)) {
                field.set(bean, Enum.valueOf((Class<? extends Enum>) fClazz, (String) value));
            } else if (fClazz.equals(vClazz) || fClazz.isAssignableFrom(vClazz)) {
                field.set(bean, value);
            } else {
                if (isPrimitiveType(vClazz) && isPrimitiveType(fClazz)) {
                    field.set(bean, JsonHelper.parseObject(JsonHelper.toJSONBytes(value), fClazz));
                } else {
                    LOG.warn("value type {} can not cast to field type {}", vClazz, fClazz);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 取对象字段值
     **/
    public static Object getProperty(final Object bean, final String name) {
        try {
            Field field = FieldUtils.getField(bean.getClass(), name, true);
            return field.get(bean);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 查找对象中的字段
     **/
    public static Field findField(final Class<?> clz, final String fieldName) {
        ConcurrentMap<String, Field> fcMap = cfMap.get(clz);
        if (CollectsHelper.isNullOrEmpty(fcMap)) {
            fcMap = Maps.newConcurrentMap();
            return findField(clz, fieldName, fcMap);

        } else {
            Field field = fcMap.get(fieldName);
            if (null != field) {
                return field;
            }
            return findField(clz, fieldName, fcMap);
        }
    }

    /**
     * 判断是否基本类型
     **/
    public static boolean isPrimitiveType(final Class<?> clz) {
        return clz.isPrimitive()
                || isSubTypeOf(clz, Number.class, Boolean.class, Character.class, String.class, Date.class, LocalDate.class, LocalDateTime.class);
    }

    public static boolean isSubTypeOf(final Class targetClz, final Class... parentClz) {
        for (Class c : parentClz) {
            if (c.isAssignableFrom(targetClz)) {
                return true;
            }
        }
        return false;
    }

    public static Object convertTypeValue(final String value, final Class clazz, final String dateFormat) {
        if (Boolean.class.equals(clazz)) {
            return Boolean.valueOf(value);
        } else if (ClassUtils.primitiveToWrapper(clazz).equals(Character.class)) {
            return value.charAt(0);
        } else if (Integer.class.isAssignableFrom(ClassUtils.primitiveToWrapper(clazz))) {
            return value.indexOf(".") != -1 ? Double.valueOf(value).intValue() : Integer.parseInt(value);
        } else if (Long.class.isAssignableFrom(ClassUtils.primitiveToWrapper(clazz))) {
            return value.indexOf(".") != -1 ? Double.valueOf(value).longValue() : Long.parseLong(value);
        } else if (Double.class.isAssignableFrom(ClassUtils.primitiveToWrapper(clazz))) {
            return Double.valueOf(value);
        } else if (String.class.equals(clazz)) {
            return value.trim();
        } else if (clazz.equals(BigDecimal.class)) {
            return new BigDecimal(value);
        } else if (Date.class.isAssignableFrom(clazz)) {
            return DateHelper.ofDate(value, Enum.valueOf(DateFormat.class, dateFormat));
        } else if (StringEnum.class.isAssignableFrom(clazz) || Enum.class.isAssignableFrom(clazz)) {
            return Enum.valueOf(clazz, value);
        } else {
            throw new RuntimeException("unknown data type: " + clazz);
        }
    }

    private static Field findField(final Class<?> clz, final String fieldName, final ConcurrentMap<String, Field> fcMap) {
        Field field = null;
        try {
            field = clz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            LOG.trace("class: {} covert field {} error ", clz.getName(), fieldName, e);
            if (clz.getSuperclass() != null) {
                field = findField(clz.getSuperclass(), fieldName);
            }
        }
        if (null != field) {
            fcMap.put(fieldName, field);
            cfMap.put(clz, fcMap);
        }
        return field;
    }

    private static boolean requireClone(final Class<?> type) {
        return !isPrimitiveType(type);
    }
}
