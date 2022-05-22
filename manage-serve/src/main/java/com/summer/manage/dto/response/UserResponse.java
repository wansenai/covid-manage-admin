package com.summer.manage.dto.response;


import com.summer.common.core.BaseDTO;

import java.util.Date;


/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/30 10:42 上午
 **/
public class UserResponse extends BaseDTO {

    /**
     * 主键ID
     **/
    public Long id;

    public String nickName;
    public String userName;

    public String deptName;

    public String phoneNumber;

    public String status;

    /**
     * 创建时间
     **/
    public Date createdAt;
    /**
     * 更新时间
     **/
    public Date updatedAt;

    public String postName;
    public String roleName;
}
