package com.summer.common.core;

import com.summer.common.ibatis.DynamicStrategy;
import com.summer.common.helper.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/** Entity基类 **/
public abstract class BaseEntity<DS extends DynamicStrategy> extends CacheSerialize {
    private static final long serialVersionUID = -8416686909846848846L;
    protected final Logger LOG;
    protected BaseEntity() {
        LOG = LoggerFactory.getLogger(this.getClass());
    }

    /** 主键ID **/
    private Long id;

    /** 创建时间 **/
    public Date createdAt;
    /** 更新时间 **/
    public Date updatedAt;

    public String createdBy;

    public String updatedBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
        ofSerializableId();
    }

    @Override
    public String toString() {
        return JsonHelper.toJSONString(this);
    }

    @Override
    public void ofSerializableId() {
        super.serializableId = String.valueOf(id);
    }

    /** 增记录时的主键值回写 **/
    public void ofDBId(Long gid) {
        setId(gid);
    }
}
