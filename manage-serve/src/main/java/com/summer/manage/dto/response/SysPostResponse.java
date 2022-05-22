package com.summer.manage.dto.response;


import com.summer.common.core.BaseDTO;

import java.util.Date;

/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/30 10:42 上午
 **/
public class SysPostResponse extends BaseDTO {
    public Long id;

    public String postCode;

    public String postName;

    public Integer postSort;

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

}
