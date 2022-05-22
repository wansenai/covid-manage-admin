package com.summer.common.redis;


import com.summer.common.core.CacheSerialize;
import com.summer.common.core.StringEnum;
import com.summer.common.helper.BeanHelper;

import java.io.Serializable;
import java.util.Collection;

public final class RedisValue implements Serializable {
    private static final long serialVersionUID = -3350456243874648627L;

    public Object val;

    public VT vt;

    public RedisValue() {
    }

    public RedisValue(Object val, VT vt) {
        this.val = val;
        this.vt = vt;
    }

    public static RedisValue newborn(Object val) {
        Class<?> clazz = val.getClass();
        if (BeanHelper.isPrimitiveType(clazz)) {
            return new RedisValue(val, VT.Primitive);
        } else {
            if (!CacheSerialize.class.isAssignableFrom(clazz)) {
                throw new RedisException("Redis cache object must extends " + CacheSerialize.class.getName());
            }
            if (Collection.class.isAssignableFrom(clazz)) {
                return new RedisValue(val, VT.Collection);
            } else {
                return new RedisValue(val, VT.Composite);
            }
        }
    }

    public enum VT implements StringEnum {
        Primitive("1"),
        Composite("2"),
        Collection("3");

        VT(String type) {
            changeNameTo(this, type);
        }
    }
}
