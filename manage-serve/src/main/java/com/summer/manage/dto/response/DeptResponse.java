package com.summer.manage.dto.response;


import com.google.common.collect.Lists;
import com.summer.common.core.BaseDTO;

import java.util.Date;
import java.util.List;

/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/27 6:03 下午
 **/
public class DeptResponse extends BaseDTO {
    /**
     * 部门ID
     */
    public Long id;

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
     * 创建时间
     **/
    public Date createdAt;
    /**
     * 更新时间
     **/
    public Date updatedAt;

    public String createdBy;

    public String updatedBy;

    /**
     * 子部门
     */
    public List<DeptResponse> children = Lists.newArrayList();
}
