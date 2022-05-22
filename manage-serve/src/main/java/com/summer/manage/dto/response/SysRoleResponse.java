package com.summer.manage.dto.response;


import com.summer.common.core.BaseDTO;

import java.util.Date;

/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/30 10:42 上午
 **/
public class SysRoleResponse extends BaseDTO {
    public Long id;

    /**
     * 角色名称
     */
    public String roleName;

    /**
     * 角色权限
     */
    public String roleKey;

    /**
     * 角色排序
     */
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
     * 创建时间
     **/
    public Date createdAt;
    /**
     * 更新时间
     **/
    public Date updatedAt;

}
