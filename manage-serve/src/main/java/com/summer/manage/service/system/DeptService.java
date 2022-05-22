package com.summer.manage.service.system;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.summer.common.core.BaseService;
import com.summer.common.exception.ThinkerException;
import com.summer.common.helper.BeanHelper;
import com.summer.common.helper.MathHelper;
import com.summer.common.view.parser.RequestContext;
import com.summer.manage.annotation.DataScope;
import com.summer.manage.core.StringUtil;
import com.summer.manage.dao.system.SysDeptDAO;
import com.summer.manage.dao.system.SysRoleDAO;
import com.summer.manage.dao.system.SysUserDAO;
import com.summer.manage.dto.request.DeptRequest;
import com.summer.manage.dto.response.DeptResponse;
import com.summer.manage.dto.response.TreeSelectResponse;
import com.summer.manage.entity.system.SysDept;
import com.summer.manage.entity.system.SysRole;
import com.summer.manage.kern.CodeMSG;
import com.summer.manage.kern.IConstant;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * @Description：
 * @Author：sacher
 * @Create：2021/1/6 3:05 下午
 **/
@Service
public class DeptService extends BaseService {

    @Inject
    private SysDeptDAO sysDeptDAO;

    @Inject
    private SysRoleDAO sysRoleDAO;


    @Inject
    private SysUserDAO sysUserDAO;

    public List<TreeSelectResponse> buildDeptTreeSelect(DeptRequest deptRequest) {
        List<SysDept> depts = selectDeptList(deptRequest);
        List<SysDept> deptTrees = buildDeptTree(depts);
        return deptTrees.stream().map(TreeSelectResponse::new).collect(Collectors.toList());
    }

    private List<SysDept> buildDeptTree(List<SysDept> depts) {
        List<SysDept> returnList = Lists.newArrayList();
        List<Long> tempList = Lists.newArrayList();
        for (SysDept dept : depts) {
            tempList.add(dept.getId());
        }
        for (SysDept dept : depts) {
            // 如果是顶级节点, 遍历该父节点的所有子节点
            if (!tempList.contains(dept.parentId)) {
                recursionFn(depts, dept);
                returnList.add(dept);
            }
        }
        if (returnList.isEmpty()) {
            returnList = depts;
        }
        return returnList;
    }

    /**
     * 递归列表
     */
    private void recursionFn(List<SysDept> list, SysDept t) {
        // 得到子节点列表
        List<SysDept> childList = getChildList(list, t);
        t.children = childList;
        for (SysDept tChild : childList) {
            if (hasChild(list, tChild)) {
                recursionFn(list, tChild);
            }
        }
    }


    /**
     * 得到子节点列表
     */
    private List<SysDept> getChildList(List<SysDept> list, SysDept t) {
        List<SysDept> tList = Lists.newArrayList();
        for (SysDept n : list) {
            if (Objects.nonNull(n.parentId) && n.parentId.longValue() == t.getId().longValue()) {
                tList.add(n);
            }
        }
        return tList;
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<SysDept> list, SysDept t) {
        return getChildList(list, t).size() > 0;
    }

    @DataScope(deptAlias = "d")
    private List<SysDept> selectDeptList(DeptRequest deptRequest) {
        return sysDeptDAO.selectDeptList(deptRequest);
    }

    public Map<String, Object> roleDeptTreeSelect(Long id) {
        Map<String, Object> res = Maps.newHashMap();
        res.put("checkedKeys", selectDeptListByRoleId(id));
        res.put("depts", buildDeptTreeSelect(new DeptRequest()));
        return res;
    }

    public List<Integer> selectDeptListByRoleId(Long roleId) {
        SysRole role = sysRoleDAO.selectRoleById(roleId);
        return sysDeptDAO.selectDeptListByRoleId(roleId, role.deptCheckStrictly);
    }


    public List<DeptResponse> listDept(DeptRequest request) {
        return BeanHelper.castTo(sysDeptDAO.selectDeptList(request), DeptResponse.class);
    }

    public DeptResponse getInfo(Long deptId) {
        return BeanHelper.castTo(sysDeptDAO.selectByPrimaryKey(deptId), DeptResponse.class);
    }

    public List<DeptResponse> excludeList(Long deptId) {
        List<SysDept> sysDepts = sysDeptDAO.selectDeptList(new DeptRequest());
        sysDepts.removeIf(d -> d.getId().intValue() == deptId
                || ArrayUtils.contains(StringUtil.split(d.ancestors, ","), deptId + ""));
        return BeanHelper.castTo(sysDepts, DeptResponse.class);
    }

    public Integer add(DeptRequest request) {
        if (IConstant.Common.NOT_UNIQUE.equals(checkDeptNameUnique(request))) {
            throw new ThinkerException(CodeMSG.DeptRepeat);
        }
        SysDept sysDept = BeanHelper.castTo(request, SysDept.class);
        sysDept.createdBy = RequestContext.get().getSession().ext;
        return insertDept(sysDept);
    }

    private Integer insertDept(SysDept sysDept) {
        SysDept info = sysDeptDAO.selectByPrimaryKey(sysDept.parentId);
        // 如果父节点不为正常状态,则不允许新增子节点
        if (!IConstant.Common.DEPT_NORMAL.equals(info.status)) {
            throw new ThinkerException(CodeMSG.DeptStop);
        }
        sysDept.ancestors = (info.ancestors + "," + sysDept.parentId);
        return sysDeptDAO.insertSelective(sysDept);
    }

    private String checkDeptNameUnique(DeptRequest request) {
        SysDept sysDept = sysDeptDAO.checkDeptNameUnique(request.deptName, request.parentId);
        if (Objects.nonNull(sysDept) && sysDept.getId() != MathHelper.nvl(request.id)) {
            return IConstant.Common.NOT_UNIQUE;
        }
        return IConstant.Common.UNIQUE;
    }

    public Integer update(DeptRequest request) {
        if (IConstant.Common.NOT_UNIQUE.equals(checkDeptNameUnique(request))) {
            throw new ThinkerException(CodeMSG.DeptRepeat);
        } else if (request.parentId.equals(request.id)) {
            throw new ThinkerException(CodeMSG.DeptMy);
        } else if (StringUtil.equals(IConstant.Common.DEPT_DISABLE, request.status)
                && sysDeptDAO.selectNormalChildrenDeptById(request.id) > 0) {
            throw new ThinkerException(CodeMSG.DeptSubNo);
        }
        SysDept sysDept = BeanHelper.castTo(request, SysDept.class);
        sysDept.updatedBy = RequestContext.get().getSession().ext;
        return updateDept(sysDept);
    }

    private Integer updateDept(SysDept sysDept) {
        SysDept newParentDept = sysDeptDAO.selectByPrimaryKey(sysDept.parentId);
        SysDept oldDept = sysDeptDAO.selectByPrimaryKey(sysDept.getId());
        if (Objects.nonNull(newParentDept) && Objects.nonNull(oldDept)) {
            String newAncestors = newParentDept.ancestors + "," + newParentDept.getId();
            String oldAncestors = oldDept.ancestors;
            sysDept.ancestors = newAncestors;
            updateDeptChildren(sysDept.getId(), newAncestors, oldAncestors);
        }
        int result = sysDeptDAO.updateByPrimaryKeySelective(sysDept);
        if (IConstant.Common.DEPT_NORMAL.equals(sysDept.status)) {
            // 如果该部门是启用状态，则启用该部门的所有上级部门
            updateParentDeptStatus(sysDept);
        }
        return result;
    }

    private void updateParentDeptStatus(SysDept sysDept) {
        String updateBy = sysDept.updatedBy;
        sysDept = sysDeptDAO.selectByPrimaryKey(sysDept.getId());
        sysDept.updatedBy = updateBy;
        sysDeptDAO.updateDeptStatus(sysDept);
    }

    private void updateDeptChildren(Long id, String newAncestors, String oldAncestors) {
        List<SysDept> children = sysDeptDAO.selectChildrenDeptById(id);
        for (SysDept child : children) {
            child.ancestors = child.ancestors.replace(oldAncestors, newAncestors);
        }
        if (children.size() > 0) {
            sysDeptDAO.updateDeptChildren(children);
        }
    }

    public Integer del(Long deptId) {
        if (hasChildByDeptId(deptId)) {
            throw new ThinkerException(CodeMSG.DeptNoDel);
        }
        if (checkDeptExistUser(deptId)) {
            throw new ThinkerException(CodeMSG.DeptUserDel);
        }
        return sysDeptDAO.deleteDeptById(deptId);
    }

    private boolean checkDeptExistUser(Long deptId) {
        int result = sysDeptDAO.checkDeptExistUser(deptId);
        return result > 0;
    }

    private boolean hasChildByDeptId(Long deptId) {
        int result = sysDeptDAO.hasChildByDeptId(deptId);
        return result > 0;
    }

}
