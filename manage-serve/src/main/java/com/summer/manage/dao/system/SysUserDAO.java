package com.summer.manage.dao.system;

import com.summer.manage.entity.system.SysDept;
import com.summer.manage.entity.system.SysUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/28 12:14 上午
 **/
public interface SysUserDAO {

    SysUser getSysUserById(@Param("userId") Long userId);

    SysUser getUserByUserName(@Param("userName") String userName);

    List<SysUser> listUser(@Param("phoneNumber") String phoneNumber,
                           @Param("userName") String userName,
                           @Param("status") String status,
                           @Param("deptId") Long deptId,
                           @Param("beginTime") String beginTime,
                           @Param("endTime") String endTime,
                           @Param("params") Map<String, Object> params,
                           @Param("offset") int offset,
                           @Param("size") int size);

    Long listUserCount(@Param("phoneNumber") String phoneNumber,
                       @Param("userName") String userName,
                       @Param("status") String status,
                       @Param("deptId") Long deptId,
                       @Param("beginTime") String beginTime,
                       @Param("endTime") String endTime,
                       @Param("params") Map<String, Object> params);

    int checkUserNameUnique(@Param("userName") String userName);

    SysUser checkPhoneUnique(@Param("phoneNumber") String phoneNumber);

    SysUser checkEmailUnique(@Param("email") String email);

    int insert(SysUser sysUser);

    SysUser selectUserById(@Param("userId") Long userId);

    int updateUser(SysUser sysUser);

    int deleteUserByIds(Long[] userIds);

    Integer resetUserPwd(@Param("password") String password, @Param("id") Long id);

    int updateUserAvatar(@Param("avatar") String avatar, @Param("id") String id);

    List<SysUser> getUserByRoleId(@Param("status") int status);

    List<SysDept> getUserByDeptId(@Param("id") Long id);
}
