package com.summer.manage.aspectj;

import com.summer.common.core.BaseDTO;
import com.summer.common.helper.CollectsHelper;
import com.summer.common.helper.SpringHelper;
import com.summer.common.helper.StringHelper;
import com.summer.common.view.parser.RequestContext;
import com.summer.manage.annotation.DataScope;
import com.summer.manage.core.StringUtil;
import com.summer.manage.dao.system.SysUserDAO;
import com.summer.manage.entity.system.SysRole;
import com.summer.manage.entity.system.SysUser;
import com.summer.manage.service.system.RoleService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * 数据过滤处理
 *
 * @author wangzechun
 */
@Aspect
@Component
public class DataScopeAspect {
    /**
     * 全部数据权限
     */
    private static final String DATA_SCOPE_ALL = "1";

    /**
     * 自定数据权限
     */
    private static final String DATA_SCOPE_CUSTOM = "2";

    /**
     * 部门数据权限
     */
    private static final String DATA_SCOPE_DEPT = "3";

    /**
     * 部门及以下数据权限
     */
    private static final String DATA_SCOPE_DEPT_AND_CHILD = "4";

    /**
     * 仅本人数据权限
     */
    private static final String DATA_SCOPE_SELF = "5";

    /**
     * 数据权限过滤关键字
     */
    private static final String DATA_SCOPE = "dataScope";

    private final SysUserDAO sysUserDAO = SpringHelper.getBean(SysUserDAO.class);

    private final RoleService roleService = SpringHelper.getBean(RoleService.class);

    /**
     * 配置织入点
     */
    @Pointcut("@annotation(com.summer.manage.annotation.DataScope)")
    public void dataScopePointCut() {
    }

    @Before("dataScopePointCut()")
    public void doBefore(JoinPoint point) {
        handleDataScope(point);
    }

    protected void handleDataScope(final JoinPoint joinPoint) {
        // 获得注解
        DataScope controllerDataScope = getAnnotationLog(joinPoint);
        if (controllerDataScope == null) {
            return;
        }
        // 获取当前的用户
        SysUser sysUser = sysUserDAO.getSysUserById(Long.parseLong(RequestContext.get().getSession().uid));
        if (Objects.nonNull(sysUser)) {
            if (!sysUser.isAdmin()) {
                dataScopeFilter(joinPoint, sysUser, controllerDataScope.deptAlias(),
                                controllerDataScope.userAlias());
            }
        }
    }

    /**
     * 数据范围过滤
     *
     * @param joinPoint 切点
     * @param user      用户
     * @param userAlias 别名
     */
    public void dataScopeFilter(JoinPoint joinPoint, SysUser user, String deptAlias, String userAlias) {
        StringBuilder sqlString = new StringBuilder();
        List<SysRole> roles = roleService.getSysRoles(user.getId());
        if (!CollectsHelper.isNullOrEmpty(roles)) {
            for (SysRole role : roles) {
                String dataScope = role.dataScope;
                if (DATA_SCOPE_ALL.equals(dataScope)) {
                    sqlString = new StringBuilder();
                    break;
                } else if (DATA_SCOPE_CUSTOM.equals(dataScope)) {
                    sqlString.append(StringUtil.format(
                            " OR {}.id IN ( SELECT dept_id FROM sys_role_dept WHERE role_id = {} ) ", deptAlias,
                            role.getId()));
                } else if (DATA_SCOPE_DEPT.equals(dataScope)) {
                    sqlString.append(StringUtil.format(" OR {}.id = {} ", deptAlias, user.deptId));
                } else if (DATA_SCOPE_DEPT_AND_CHILD.equals(dataScope)) {
                    sqlString.append(StringUtil.format(
                            " OR {}.id IN ( SELECT id FROM sys_dept WHERE id = {} OR FIND_IN_SET( {} , ancestors ) )",
                            deptAlias, user.deptId, user.deptId));
                } else if (DATA_SCOPE_SELF.equals(dataScope)) {
                    if (!StringHelper.isBlank(userAlias)) {
                        sqlString.append(StringUtil.format(" OR {}.id = {} ", userAlias, user.getId()));
                    } else {
                        // 数据权限为仅本人且没有userAlias别名不查询任何数据
                        sqlString.append(" OR 1=0 ");
                    }
                }
            }
        }

        if (!StringHelper.isBlank(sqlString.toString())) {
            Object params = joinPoint.getArgs()[0];
            if (Objects.nonNull(params) && params instanceof BaseDTO) {
                BaseDTO baseEntity = (BaseDTO) params;
                baseEntity.getParams().put(DATA_SCOPE, " AND (" + sqlString.substring(4) + ")");
            }
        }
    }

    /**
     * 是否存在注解，如果存在就获取
     */
    private DataScope getAnnotationLog(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (method != null) {
            return method.getAnnotation(DataScope.class);
        }
        return null;
    }
}
