package com.summer.common.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.summer.common.helper.BeanHelper;
import com.summer.common.ibatis.DynamicStrategy;
import com.summer.common.helper.CollectsHelper;
import com.summer.common.helper.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public abstract class BaseDTO implements Serializable {
    private static final long serialVersionUID = 8313416100952862853L;
    protected final Logger LOG;

    public Map<String, Object> params;

    protected BaseDTO() {
        LOG = LoggerFactory.getLogger(this.getClass());
    }

    public static <T> T fromEntity(BaseEntity<? extends DynamicStrategy> entity, Class<T> clazz) {
        return BeanHelper.castTo(entity, clazz);
    }

    public static <T> List<T> fromEntity(List<? extends BaseEntity<? extends DynamicStrategy>> entities, Class<T> clazz) {
        if(CollectsHelper.isNullOrEmpty(entities)) {
            return Lists.newArrayList();
        }
        return BeanHelper.castTo(entities, clazz);
    }

    public static <T extends BaseEntity<? extends DynamicStrategy>> T toEntity(BaseDTO dto, Class<T> clazz) {
        return BeanHelper.castTo(dto, clazz);
    }

    public static <T extends BaseEntity<? extends DynamicStrategy>> List<T> toEntity(List<? extends BaseDTO> dtos, Class<T> clazz) {
        if(CollectsHelper.isNullOrEmpty(dtos)) {
            return Lists.newArrayList();
        }
        return BeanHelper.castTo(dtos, clazz);
    }

    public Map<String, Object> getParams() {
        if (params == null) {
            params = Maps.newHashMap();
        }
        return params;
    }

    @Override
    public String toString() {
        return JsonHelper.toJSONString(this);
    }

}
