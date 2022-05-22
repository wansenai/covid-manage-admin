package com.summer.manage.service.system;

import com.google.common.collect.Sets;
import com.summer.common.core.BaseService;
import com.summer.manage.entity.system.SysUser;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Set;

/**
 * @Description：
 * @Author：sacher
 * @Create：2021/1/6 3:05 下午
 **/
@Service
public class PermissionService extends BaseService {

    @Inject
    private MenuService menuService;

    @Inject
    private RoleService roleService;

    public Set<String> getMenuPermission(SysUser sysUser) {
        Set<String> perms = Sets.newHashSet();
        // 管理员拥有所有权限
        if (sysUser.isAdmin()) {
            perms.add("/");
        } else {
            perms.addAll(menuService.selectMenuPermsByUserId(sysUser.getId()));
        }
        return perms;
    }

    public Set<String> getRolePermission(SysUser sysUser) {
        Set<String> perms = Sets.newHashSet();
        // 管理员拥有所有权限
        if (sysUser.isAdmin()) {
            perms.add("admin");
        } else {
            perms.addAll(roleService.selectRolePermissionByUserId(sysUser.getId()));
        }
        return perms;
    }
}
