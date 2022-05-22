package com.summer.manage.entity.system;


import com.summer.common.core.BaseEntity;
import com.summer.common.ibatis.DefaultStrategy;

/**
 * 字典类型表 sys_dict_type
 *
 * @author sacher
 */
public class SysDictType extends BaseEntity<DefaultStrategy> {

    /**
     * 字典名称
     */
    public String dictName;

    /**
     * 字典类型
     */
    public String dictType;

    /**
     * 状态（0正常 1停用）
     */
    public String status;

    public String remark;

}
