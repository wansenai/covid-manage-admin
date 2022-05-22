package com.summer.manage.dao.system;

import com.summer.manage.dto.request.RoleRequest;
import com.summer.manage.entity.system.SysRole;
import com.summer.manage.entity.system.SysRoleDept;
import com.summer.manage.entity.system.SysRoleMenu;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/28 12:14 上午
 **/
public interface SysRoleDAO {

    List<SysRole> selectRolePermissionByUserId(@Param("id") Long id);

    List<SysRole> selectRoleList(RoleRequest roleRequest);

    List<SysRole> selectRoleListByUserId(@Param("userId") Long userId);

    List<SysRole> selectRoleListPage(@Param("roleName") String roleName,
                                     @Param("roleKey") String roleKey,
                                     @Param("status") String status,
                                     @Param("beginTime") String beginTime,
                                     @Param("endTime") String endTime,
                                     @Param("params") Map<String, Object> params,
                                     @Param("offset") int offset,
                                     @Param("size") int size);

    Long selectRoleListPageCount(@Param("roleName") String roleName, @Param("roleKey") String roleKey, @Param("status") String status, @Param("beginTime") String beginTime, @Param("endTime") String endTime, @Param("params") Map<String, Object> params);

    SysRole selectRoleById(@Param("id") Long id);

    Integer update(SysRole sysRole);

    SysRole checkRoleNameUnique(@Param("roleName") String roleName);

    SysRole checkRoleKeyUnique(@Param("roleKey") String roleKey);

    void insert(SysRole sysRole);

    int batchRoleMenu(List<SysRoleMenu> list);

    int countUserRoleByRoleId(@Param("roleId") Long roleId);

    Integer deleteRoleByIds(Long[] roleIds);

    void deleteRoleDeptByRoleId(@Param("id") Long id);

    int batchRoleDept(List<SysRoleDept> list);

    void deleteRoleMenuByRoleId(@Param("id") Long id);
}
