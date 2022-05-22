package com.summer.manage.entity.system;



import com.summer.common.core.BaseEntity;
import com.summer.common.ibatis.DefaultStrategy;

import java.util.Date;
import java.util.List;

/**
 * @ClassName SysUser
 * @Description:
 * @Author Sacher
 * @Date 2020/11/26
 **/
public class SysUser extends BaseEntity<DefaultStrategy> {
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
    public String deptName;
    public SysDept dept;
    /**
     * 角色对象
     */
    public List<SysRole> roles;
    /**
     * 角色组
     */
    public List<Long> roleIds;
    /**
     * 岗位组
     */
    public List<Long> postIds;

    public SysUser(Long id) {
        super.setId(id);
    }

    public SysUser() {
    }

    public static boolean isAdmin(Long userId) {
        return userId != null && 1L == userId;
    }

    public boolean isAdmin() {
        return isAdmin(this.getId());
    }
}
