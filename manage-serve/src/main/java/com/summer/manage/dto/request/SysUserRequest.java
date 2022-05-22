package com.summer.manage.dto.request;


import com.summer.common.core.BaseDTO;
import com.summer.common.core.IRequest;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

/**
 * @Description：
 * @Author：sacher
 * @Create：2021/1/7 11:30 上午
 **/
public class SysUserRequest extends BaseDTO implements IRequest {
    public Long id;
    public Long deptId;
    public String avatar;
    @Size(min = 0, max = 30, message = "用户昵称长度不能超过30个字符")
    public String nickName;
    @NotBlank(message = "用户账号不能为空")
    @Size(min = 0, max = 30, message = "用户账号长度不能超过30个字符")
    public String userName;
    public String userPassword;
    @Email(message = "邮箱格式不正确")
    @Size(max = 50, message = "邮箱长度不能超过50个字符")
    public String email;
    @Size(max = 11, message = "手机号码长度不能超过11个字符")
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
    /**
     * 角色组
     */
    public List<Long> roleIds;
    /**
     * 岗位组
     */
    public List<Long> postIds;

    @Override
    public void verify() {

    }
}
