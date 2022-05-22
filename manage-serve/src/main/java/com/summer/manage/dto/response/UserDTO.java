package com.summer.manage.dto.response;

import com.summer.common.core.BaseDTO;
import com.summer.manage.entity.system.SysDept;

import java.util.Date;

/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/28 3:41 下午
 **/
public class UserDTO extends BaseDTO {
    public Long id;
    public Long deptId;

    public String avatar;
    public String nickName;
    public String userName;
    public String userPassword;
    public String email;
    public String phoneNumber;
    /**
     * 用户性别（0男 1女 2未知）
     */
    public String sex;

    /**
     * 账户状态 0：正常 ，1：异常
     */
    public String status;

    /**
     * 账户状态 0未删除 2已删除
     */
    public String isDel;

    public String loginIp;

    public Date loginDate;

    public String remark;

    public SysDept dept;

    public Date createdAt;
}
