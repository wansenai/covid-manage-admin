package com.summer.manage.dao.system;


import com.summer.manage.entity.system.SysUserPost;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/28 12:14 上午
 **/
public interface UserPostDAO {

    void batchUserPost(List<SysUserPost> list);

    void deleteUserPostByUserId(@Param("userId") Long userId);

    void deleteUserPost(Long[] userIds);
}
