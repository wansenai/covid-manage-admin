package com.summer.manage.service.system;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.summer.common.core.BaseEntity;
import com.summer.common.core.BaseService;
import com.summer.common.exception.ThinkerException;
import com.summer.common.helper.BeanHelper;
import com.summer.common.helper.MathHelper;
import com.summer.common.helper.SpringHelper;
import com.summer.common.support.Pagination;
import com.summer.common.view.parser.RequestContext;
import com.summer.manage.annotation.DataScope;
import com.summer.manage.dao.system.SysRoleDAO;
import com.summer.manage.dto.request.RoleListRequest;
import com.summer.manage.dto.request.RoleRequest;
import com.summer.manage.dto.response.SysRoleResponse;
import com.summer.manage.entity.system.SysRole;
import com.summer.manage.entity.system.SysRoleDept;
import com.summer.manage.entity.system.SysRoleMenu;
import com.summer.manage.kern.CodeMSG;
import com.summer.manage.kern.IConstant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Description：
 * @Author：sacher
 * @Create：2021/1/6 3:05 下午
 **/
@Service
public class RoleService extends BaseService {

    @Inject
    private SysRoleDAO sysRoleDAO;

    public Set<String> selectRolePermissionByUserId(Long id) {
        Set<String> result = Sets.newHashSet();
        List<SysRole> perms = getSysRoles(id);
        for (SysRole perm : perms) {
            if (Objects.nonNull(perm)) {
                result.addAll(Arrays.asList(perm.roleKey.trim().split(",")));
            }
        }
        return result;
    }

    public List<SysRole> getSysRoles(Long id) {
        return sysRoleDAO.selectRolePermissionByUserId(id);
    }

    public List<SysRole> selectRoleAll() {
        return SpringHelper.getAopProxy(this).selectRoleList(new RoleRequest());
    }

    @DataScope(deptAlias = "d")
    public List<SysRole> selectRoleList(RoleRequest roleRequest) {
        return sysRoleDAO.selectRoleList(roleRequest);
    }

    public List<SysRole> selectRoleListByUserId(Long userId) {
        return sysRoleDAO.selectRoleListByUserId(userId);
    }

    public List<Long> selectRoleIdListByUserId(Long userId) {
        return selectRoleListByUserId(userId).stream().map(BaseEntity::getId).collect(Collectors.toList());
    }

    @DataScope(deptAlias = "d")
    public Pagination<SysRoleResponse> listRole(RoleListRequest request) {
        Pagination<SysRoleResponse> pagination = Pagination.create(request.pager, request.size);
        List<SysRole> selectRoleList = sysRoleDAO.selectRoleListPage(request.roleName,
                                                                     request.roleKey,
                                                                     request.status,
                                                                     request.beginTime,
                                                                     request.endTime,
                                                                     request.params,
                                                                     pagination.getOffset(),
                                                                     pagination.getSize());
        List<SysRoleResponse> sysRoleResponses = BeanHelper.castTo(selectRoleList, SysRoleResponse.class);
        pagination.getList().addAll(sysRoleResponses);
        Long total = sysRoleDAO.selectRoleListPageCount(request.roleName,
                                                        request.roleKey,
                                                        request.status,
                                                        request.beginTime,
                                                        request.endTime,
                                                        request.params);
        pagination.setTotal(null == total ? 0L : total);
        return pagination;
    }

    public SysRoleResponse getInfo(Long id) {
        return BeanHelper.castTo(sysRoleDAO.selectRoleById(id), SysRoleResponse.class);
    }

    public Integer changeStatus(RoleRequest request) {
        SysRole sysRole = BeanHelper.castTo(request, SysRole.class);
        checkRoleAllowed(sysRole);
        sysRole.updatedBy = RequestContext.get().getSession().ext;
        return sysRoleDAO.update(sysRole);
    }

    private void checkRoleAllowed(SysRole sysRole) {
        if (sysRole.isAdmin()) {
            throw new ThinkerException(CodeMSG.AdminNo);
        }
    }

    @Transactional
    public Integer add(RoleRequest request) {
        if (IConstant.Common.NOT_UNIQUE.equals(checkRoleNameUnique(request))) {
            throw new ThinkerException(CodeMSG.RoleRepeat);
        } else if (IConstant.Common.NOT_UNIQUE.equals(checkRoleKeyUnique(request))) {
            throw new ThinkerException(CodeMSG.RoleLimitRepeat);
        }
        SysRole sysRole = BeanHelper.castTo(request, SysRole.class);
        sysRole.createdBy = RequestContext.get().getSession().ext;
        return insertRole(sysRole);
    }

    private Integer insertRole(SysRole sysRole) {
        // 新增角色信息
        sysRoleDAO.insert(sysRole);
        return insertRoleMenu(sysRole);
    }

    private Integer insertRoleMenu(SysRole sysRole) {
        int rows = 1;
        // 新增用户与角色管理
        List<SysRoleMenu> list = Lists.newArrayList();
        for (Long menuId : sysRole.menuIds) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.roleId = sysRole.getId();
            rm.menuId = menuId;
            list.add(rm);
        }
        if (list.size() > 0) {
            rows = sysRoleDAO.batchRoleMenu(list);
        }
        return rows;
    }

    private String checkRoleKeyUnique(RoleRequest request) {
        SysRole sysRole = sysRoleDAO.checkRoleNameUnique(request.roleName);
        if (Objects.nonNull(sysRole) && sysRole.getId() != MathHelper.nvl(request.id)) {
            return IConstant.Common.NOT_UNIQUE;
        }
        return IConstant.Common.UNIQUE;
    }

    private String checkRoleNameUnique(RoleRequest request) {
        SysRole sysRole = sysRoleDAO.checkRoleKeyUnique(request.roleKey);
        if (Objects.nonNull(sysRole) && sysRole.getId() != MathHelper.nvl(request.id)) {
            return IConstant.Common.NOT_UNIQUE;
        }
        return IConstant.Common.UNIQUE;
    }

    public Integer del(Long[] roleIds) {
        for (Long roleId : roleIds) {
            SysRole sysRole = new SysRole();
            sysRole.setId(roleId);
            checkRoleAllowed(sysRole);
            SysRole role = sysRoleDAO.selectRoleById(roleId);
            if (sysRoleDAO.countUserRoleByRoleId(roleId) > 0) {
                throw new ThinkerException(CodeMSG.Common.code(), role.roleName);
            }
        }
        return sysRoleDAO.deleteRoleByIds(roleIds);
    }

    public Integer update(RoleRequest request) {
        SysRole sysRole = BeanHelper.castTo(request, SysRole.class);
        checkRoleAllowed(sysRole);
        if (IConstant.Common.NOT_UNIQUE.equals(checkRoleNameUnique(request))) {
            throw new ThinkerException(CodeMSG.RoleRepeat);
        } else if (IConstant.Common.NOT_UNIQUE.equals(checkRoleKeyUnique(request))) {
            throw new ThinkerException(CodeMSG.RoleLimitRepeat);
        }
        sysRole.updatedBy = RequestContext.get().getSession().ext;
        return updateRole(sysRole);
    }

    private Integer updateRole(SysRole sysRole) {
        sysRoleDAO.update(sysRole);
        // 删除角色与菜单关联
        sysRoleDAO.deleteRoleMenuByRoleId(sysRole.getId());
        return insertRoleMenu(sysRole);
    }

    public Integer dataScope(RoleRequest request) {
        SysRole sysRole = BeanHelper.castTo(request, SysRole.class);
        checkRoleAllowed(sysRole);
        return authDataScope(sysRole);
    }

    private Integer authDataScope(SysRole sysRole) {
        // 修改角色信息
        sysRoleDAO.update(sysRole);
        // 删除角色与部门关联
        sysRoleDAO.deleteRoleDeptByRoleId(sysRole.getId());
        // 新增角色和部门信息（数据权限）
        return insertRoleDept(sysRole);
    }

    private Integer insertRoleDept(SysRole sysRole) {
        int rows = 1;
        // 新增角色与部门（数据权限）管理
        List<SysRoleDept> list = Lists.newArrayList();
        for (Long deptId : sysRole.deptIds) {
            SysRoleDept rd = new SysRoleDept();
            rd.roleId = sysRole.getId();
            rd.deptId = deptId;
            list.add(rd);
        }
        if (list.size() > 0) {
            rows = sysRoleDAO.batchRoleDept(list);
        }
        return rows;
    }
}
