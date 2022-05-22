package com.summer.manage.entity.system;

import com.google.common.collect.Lists;
import com.summer.common.core.BaseEntity;
import com.summer.common.ibatis.DefaultStrategy;

import java.util.List;


/**
 * sys_dept
 *
 * @author Sacher
 */
public class SysDept extends BaseEntity<DefaultStrategy> {

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
    public String deptName;

    /**
     * 显示顺序
     */
    public Integer orderNum;

    /**
     * 负责人
     */
    public String leader;

    /**
     * 联系电话
     */
    public String phone;

    /**
     * 邮箱
     */
    public String email;

    /**
     * 部门状态（0正常 1停用）
     */
    public String status;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    public String isDel;

    /**
     * 子部门
     */
    public List<SysDept> children = Lists.newArrayList();

}
