package com.summer.manage.entity.system;


import com.summer.common.core.BaseEntity;
import com.summer.common.ibatis.DefaultStrategy;

/**
 * sys_dept
 *
 * @author Sacher
 */
public class SysPost extends BaseEntity<DefaultStrategy> {

    public String postCode;

    public String postName;

    public Integer postSort;

    public String status;

    public String remark;

}
