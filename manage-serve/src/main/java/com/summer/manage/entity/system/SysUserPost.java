package com.summer.manage.entity.system;


import com.summer.common.core.BaseEntity;
import com.summer.common.ibatis.DefaultStrategy;

/**
 * @ClassName SysUser
 * @Description:
 * @Author Sacher
 * @Date 2020/11/26
 **/
public class SysUserPost extends BaseEntity<DefaultStrategy> {
    /**
     * 用户ID
     */
    public Long userId;

    /**
     * 岗位ID
     */
    public Long postId;

}
