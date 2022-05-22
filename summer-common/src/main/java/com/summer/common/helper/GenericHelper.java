package com.summer.common.helper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 获取泛型
 **/
public final class GenericHelper {
    private GenericHelper() {
    }

    public static Class<?> type(final Type gt) {
        return type(gt, 0);
    }

    public static Class type(final Class clz) {
        return type(clz, 0);
    }

    public static Class type(final Class clz, int index) {
        Type genType = clz.getGenericSuperclass();
        if (genType == null) {
            for (Type type : clz.getGenericInterfaces()) {
                if (type != null) {
                    genType = type;
                }
            }
        }
        return type(genType, index);
    }

    public static Class<?> type(final Type gt, int index) {
        // 如果没有实现ParameterizedType接口，即不支持泛型，直接返回Object.class
        if (!(gt instanceof ParameterizedType)) {
            return Object.class;
        }
        // 返回表示此类型实际类型参数的Type对象的数组,数组里放的都是对应类型的Class
        Type[] params = ((ParameterizedType) gt).getActualTypeArguments();
        if (index >= params.length || index < 0) {
            throw new RuntimeException("你输入的索引" + (index < 0 ? "不能小于0" : "超出了参数的总数"));
        }
        Type param = params[index];
        if (!(param instanceof Class)) {
            if (param instanceof ParameterizedType) {
                Type rawType = ((ParameterizedType) param).getRawType();
                if (rawType instanceof Class) {
                    return (Class<?>) rawType;
                }
            }
            return Object.class;
        }
        return (Class<?>) param;
    }

}
