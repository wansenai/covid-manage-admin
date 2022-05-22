package com.summer.manage.dto.response;


import com.summer.common.core.BaseDTO;

import java.util.Date;


/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/30 10:42 上午
 **/
public class GenTableResponse extends BaseDTO {

    /**
     * 主键ID
     **/
    public Long id;

    /**
     * 创建时间
     **/
    public Date createdAt;
    /**
     * 更新时间
     **/
    public Date updatedAt;

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

}
