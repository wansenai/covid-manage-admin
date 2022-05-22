package com.summer.manage.dao.system;


import com.summer.manage.entity.system.SysPost;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wangzechun
 */
public interface SysPostDAO {

    List<SysPost> selectPostAll();

    List<SysPost> selectPostListByUserId(@Param("userId") Long userId);

    List<SysPost> selectPostList(@Param("postCode") String postCode, @Param("postName") String postName, @Param("status") String status, @Param("offset") int offset, @Param("size") int size);

    Long selectPostListCount(@Param("postCode") String postCode, @Param("postName") String postName, @Param("status") String status);

    SysPost selectPostById(@Param("id") Long id);

    SysPost checkPostNameUnique(@Param("postName") String postName);

    SysPost checkPostCodeUnique(@Param("postCode") String postCode);

    Integer insertPost(SysPost sysPost);

    Integer updatePost(SysPost sysPost);

    int countUserPostById(@Param("postId") Long postId);

    Integer deletePostByIds(Long[] postIds);
}
