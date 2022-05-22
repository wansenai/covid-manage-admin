package com.summer.manage.entity.system;

import com.summer.common.core.BaseEntity;
import com.summer.common.ibatis.DefaultStrategy;

import java.util.List;


/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/30 10:42 上午
 **/
public class GenTable extends BaseEntity<DefaultStrategy> {

    /**
     * 数据库名称
     */
    public String dbName;

    /**
     * 表名称
     */
    public String tableName;

    /**
     * 表描述
     */
    public String tableComment;

    /**
     * 实体类名称(首字母大写)
     */
    public String className;

    /**
     * 使用的模板（crud单表操作 tree树表操作）
     */
    public String tplCategory;

    /**
     * 生成包路径
     */
    public String packageName;

    /**
     * 生成模块名
     */
    public String moduleName;

    /**
     * 生成业务名
     */
    public String businessName;

    /**
     * 生成功能名
     */
    public String functionName;

    /**
     * 生成作者
     */
    public String functionAuthor;

    /**
     * 生成代码方式（0zip压缩包 1自定义路径）
     */
    public String genType;

    /**
     * 生成路径（不填默认项目路径）
     */
    public String genPath;

    /**
     * 创建者
     */
    public String createBy;

    /**
     * 更新者
     */
    public String updateBy;

    /**
     * 备注
     */
    public String remark;

    /**
     * 其它生成选项
     */
    public String options;

    /**
     * 表列信息
     */
    public List<GenTableColumn> columns;

    /**
     * 主键信息
     */
    public GenTableColumn pkColumn;

}
