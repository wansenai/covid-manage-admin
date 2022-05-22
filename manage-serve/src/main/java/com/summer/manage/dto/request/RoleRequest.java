package com.summer.manage.dto.request;


import com.summer.common.core.BaseDTO;
import com.summer.common.core.IRequest;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @Description：
 * @Author：sacher
 * @Create：2021/1/7 3:52 下午
 **/
public class RoleRequest extends BaseDTO implements IRequest {
    public Long id;

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    @Size(min = 0, max = 30, message = "角色名称长度不能超过30个字符")
    public String roleName;

    /**
     * 角色权限
     */
    @NotBlank(message = "权限字符不能为空")
    @Size(min = 0, max = 100, message = "权限字符长度不能超过100个字符")
    public String roleKey;

    /**
     * 角色排序
     */
    @NotBlank(message = "显示顺序不能为空")
    public String roleSort;

    /**
     * 数据范围（1：所有数据权限；2：自定义数据权限；3：本部门数据权限；4：本部门及以下数据权限）
     */
    public String dataScope;

    /**
     * 菜单树选择项是否关联显示（ 0：父子不互相关联显示 1：父子互相关联显示）
     */
    public boolean menuCheckStrictly;

    /**
     * 部门树选择项是否关联显示（0：父子不互相关联显示 1：父子互相关联显示 ）
     */
    public boolean deptCheckStrictly;

    /**
     * 角色状态（0正常 1停用）
     */
    public String status;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    public String isDel;

    public String remark;

    /**
     * 菜单组
     */
    public Long[] menuIds;

    /**
     * 部门组（数据权限）
     */
    public Long[] deptIds;

    @Override
    public void verify() {

    }
}
