package com.summer.manage.dao.system;


import com.summer.manage.entity.system.SysUserRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/28 12:14 上午
 **/
public interface UserRoleDAO {

    void batchUserRole(List<SysUserRole> list);

    void deleteUserRoleByUserId(@Param("userId") Long userId);

    void deleteUserRole(Long[] userIds);
}
