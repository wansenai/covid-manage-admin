package com.summer.manage.entity.system;

import com.summer.common.core.BaseEntity;
import com.summer.common.ibatis.DefaultStrategy;
import com.summer.manage.core.StringUtil;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;


/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/30 10:42 上午
 **/
public class GenTableColumn extends BaseEntity<DefaultStrategy> {

    /**
     * 创建者
     */
    public String createBy;

    /**
     * 更新者
     */
    public String updateBy;

    /**
     * 归属表编号
     */
    public Long tableId;

    /**
     * 列名称
     */
    public String columnName;

    /**
     * 列描述
     */
    public String columnComment;

    /**
     * 列类型
     */
    public String columnType;

    /**
     * JAVA类型
     */
    public String javaType;

    /**
     * JAVA字段名
     */
    @NotBlank(message = "Java属性不能为空")
    public String javaField;

    /**
     * 是否主键（1是）
     */
    public String isPk;

    /**
     * 是否自增（1是）
     */
    public String isIncrement;

    /**
     * 是否必填（1是）
     */
    public String isRequired;

    /**
     * 是否为插入字段（1是）
     */
    public String isInsert;

    /**
     * 是否编辑字段（1是）
     */
    public String isEdit;

    /**
     * 是否列表字段（1是）
     */
    public String isList;

    /**
     * 是否查询字段（1是）
     */
    public String isQuery;

    /**
     * 查询方式（EQ等于、NE不等于、GT大于、LT小于、LIKE模糊、BETWEEN范围）
     */
    public String queryType;

    /**
     * 显示类型（input文本框、textarea文本域、select下拉框、checkbox复选框、radio单选框、datetime日期控件、editor富文本控件）
     */
    public String htmlType;

    /**
     * 字典类型
     */
    public String dictType;

    /**
     * 排序
     */
    public Integer sort;

    public static boolean isSuperColumn(String javaField) {
        return StringUtils.equalsAnyIgnoreCase(javaField,
                                               // BaseEntity
                                               "createdAt", "updatedAt", "id",
                                               // TreeEntity
                                               "parentName", "parentId", "orderNum", "ancestors");
    }

    public boolean isPk() {
        return isPk(this.isPk);
    }

    public boolean isPk(String isPk) {
        return isPk != null && StringUtil.equals("1", isPk);
    }

    public boolean isSuperColumn() {
        return isSuperColumn(this.javaField);
    }

    public boolean isList() {
        return isList(this.isList);
    }

    public boolean isList(String isList) {
        return isList != null && StringUtil.equals("1", isList);
    }

}
