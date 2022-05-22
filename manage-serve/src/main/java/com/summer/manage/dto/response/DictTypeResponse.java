package com.summer.manage.dto.response;

import java.util.Date;

/**
 * @Description：
 * @Author：sacher
 * @Create：2021/1/9 9:47 下午
 **/
public class DictTypeResponse {

    public Long id;

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

    /**
     * 创建时间
     **/
    public Date createdAt;
    /**
     * 更新时间
     **/
    public Date updatedAt;

    public String createdBy;

    public String updatedBy;
}
