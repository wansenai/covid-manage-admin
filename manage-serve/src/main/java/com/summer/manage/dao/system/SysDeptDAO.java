package com.summer.manage.dao.system;

import com.summer.manage.dto.request.DeptRequest;
import com.summer.manage.entity.system.SysDept;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysDeptDAO {
    int deleteByPrimaryKey(@Param("id") Long id);

    int insert(SysDept record);

    int insertSelective(SysDept record);

    SysDept selectByPrimaryKey(@Param("id") Long id);

    int updateByPrimaryKeySelective(SysDept record);

    int updateByPrimaryKey(SysDept record);

    List<SysDept> selectDeptList(DeptRequest deptRequest);

    List<Integer> selectDeptListByRoleId(@Param("roleId") Long roleId, @Param("deptCheckStrictly") boolean deptCheckStrictly);

    SysDept checkDeptNameUnique(@Param("deptName") String deptName, @Param("parentId") Long parentId);

    int selectNormalChildrenDeptById(@Param("id") Long id);

    List<SysDept> selectChildrenDeptById(@Param("id") Long id);

    void updateDeptChildren(@Param("children") List<SysDept> children);

    void updateDeptStatus(SysDept sysDept);

    int hasChildByDeptId(@Param("deptId") Long deptId);

    int checkDeptExistUser(@Param("deptId") Long deptId);

    int deleteDeptById(@Param("deptId") Long deptId);
}
