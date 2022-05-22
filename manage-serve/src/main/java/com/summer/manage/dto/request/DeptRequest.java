package com.summer.manage.dto.request;


import com.summer.common.core.BaseDTO;
import com.summer.common.core.IRequest;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @Description：
 * @Author：sacher
 * @Create：2021/1/7 3:52 下午
 **/
public class DeptRequest extends BaseDTO implements IRequest {
    public Long id;
    /**
     * 父部门id
     */
    public Long parentId;

    /**
     * 祖级列表
     */
    public String ancestors;

    /**
     * 部门名称
     */
    @NotBlank(message = "部门名称不能为空")
    @Size(min = 0, max = 30, message = "部门名称长度不能超过30个字符")
    public String deptName;

    /**
     * 显示顺序
     */
    @NotNull(message = "显示顺序不能为空")
    public Integer orderNum;

    /**
     * 负责人
     */
    public String leader;

    /**
     * 联系电话
     */
    @Size(max = 11, message = "联系电话长度不能超过11个字符")
    public String phone;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Size(max = 50, message = "邮箱长度不能超过50个字符")
    public String email;

    /**
     * 部门状态（0正常 1停用）
     */
    public String status;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    public String isDel;

    @Override
    public void verify() {

    }
}
