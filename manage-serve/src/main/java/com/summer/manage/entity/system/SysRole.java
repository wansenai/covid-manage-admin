package com.summer.manage.entity.system;


import com.summer.common.core.BaseEntity;
import com.summer.common.ibatis.DefaultStrategy;

/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/30 10:42 上午
 **/
public class SysRole extends BaseEntity<DefaultStrategy> {

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
     * 菜单组
     */
    public Long[] menuIds;

    /**
     * 部门组（数据权限）
     */
    public Long[] deptIds;

    public static boolean isAdmin(Long roleId) {
        return roleId != null && 1L == roleId;
    }

    public boolean isAdmin() {
        return isAdmin(this.getId());
    }

}
