package com.summer.manage.entity.system;


import com.summer.common.core.BaseEntity;
import com.summer.common.ibatis.DefaultStrategy;

/**
 * 字典数据表 sys_dict_data
 *
 * @author sacher
 */
public class SysDictData extends BaseEntity<DefaultStrategy> {

    /**
     * 字典排序
     */
    public Long dictSort;

    /**
     * 字典标签
     */
    public String dictLabel;

    /**
     * 字典键值
     */
    public String dictValue;

    /**
     * 字典类型
     */
    public String dictType;

    /**
     * 样式属性（其他样式扩展）
     */
    public String cssClass;

    /**
     * 表格字典样式
     */
    public String listClass;

    /**
     * 是否默认（Y是 N否）
     */
    public String isDefault;

    /**
     * 状态（0正常 1停用）
     */
    public String status;

    public String remark;


}
