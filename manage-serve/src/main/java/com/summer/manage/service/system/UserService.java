package com.summer.manage.service.system;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.summer.common.core.BaseService;
import com.summer.common.exception.ThinkerException;
import com.summer.common.helper.BeanHelper;
import com.summer.common.helper.CollectsHelper;
import com.summer.common.helper.MathHelper;
import com.summer.common.helper.StringHelper;
import com.summer.common.support.Pagination;
import com.summer.common.view.parser.RequestContext;
import com.summer.manage.annotation.DataScope;
import com.summer.manage.core.LoginUtil;
import com.summer.manage.core.file.FileUploadUtils;
import com.summer.manage.dao.system.SysUserDAO;
import com.summer.manage.dao.system.UserPostDAO;
import com.summer.manage.dao.system.UserRoleDAO;
import com.summer.manage.dto.request.SysUserRequest;
import com.summer.manage.dto.request.UserListRequest;
import com.summer.manage.dto.response.MyProfileResponse;
import com.summer.manage.dto.response.UserDTO;
import com.summer.manage.dto.response.UserResponse;
import com.summer.manage.entity.system.SysPost;
import com.summer.manage.entity.system.SysRole;
import com.summer.manage.entity.system.SysUser;
import com.summer.manage.entity.system.SysUserPost;
import com.summer.manage.entity.system.SysUserRole;
import com.summer.manage.kern.CodeMSG;
import com.summer.manage.kern.IConstant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.util.HashMap;
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
public class UserService extends BaseService {

    @Inject
    private SysUserDAO sysUserDAO;

    @Inject
    private UserPostDAO userPostDAO;

    @Inject
    private UserRoleDAO userRoleDAO;

    @Inject
    private RoleService roleService;

    @Inject
    private PostService postService;

    @DataScope(deptAlias = "d", userAlias = "u")
    public Pagination<UserResponse> listUser(UserListRequest request) {
        Pagination<UserResponse> pagination = Pagination.create(request.pager, request.size);
        List<SysUser> users = sysUserDAO.listUser(request.phoneName(),
                                                  request.userName(),
                                                  request.status,
                                                  request.deptId,
                                                  request.beginTime,
                                                  request.endTime,
                                                  request.params,
                                                  pagination.getOffset(),
                                                  pagination.getSize());
        List<UserResponse> userResponses = BeanHelper.castTo(users, UserResponse.class);
        for (UserResponse userResponse : userResponses) {
            List<SysPost> posts = postService.selectPostListByUserId(userResponse.id);
            if (!CollectsHelper.isNullOrEmpty(posts)) {
                userResponse.postName = posts.stream().map(e -> e.postName).collect(Collectors.joining(","));
            }
            List<SysRole> sysRoles = roleService.selectRoleListByUserId(userResponse.id);
            if (!CollectsHelper.isNullOrEmpty(sysRoles)) {
                userResponse.roleName = sysRoles.stream().map(e -> e.roleName).collect(Collectors.joining(","));
            }
        }
        pagination.getList().addAll(userResponses);
        Long total = sysUserDAO.listUserCount(request.phoneName(),
                                              request.userName(),
                                              request.status,
                                              request.deptId,
                                              request.beginTime,
                                              request.endTime,
                                              request.params);
        pagination.setTotal(null == total ? 0L : total);
        return pagination;
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer add(SysUserRequest user) {
        if (sysUserDAO.checkUserNameUnique(user.userName) > 0) {
            throw new ThinkerException(CodeMSG.UserRepeat);
        }
        checkEmailAndPhone(user);
        user.userPassword = LoginUtil.adminPwd(user.userPassword);
        SysUser sysUser = BeanHelper.castTo(user, SysUser.class);
        sysUser.createdBy = RequestContext.get().getSession().ext;
        //新增用户信息
        int row = sysUserDAO.insert(sysUser);
        // 新增用户岗位关联
        insertUserPost(user, sysUser.getId());
        // 新增用户与角色管理
        insertUserRole(user, sysUser.getId());
        return row;
    }

    private void checkEmailAndPhone(SysUserRequest user) {
        if (!StringHelper.isBlank(user.phoneNumber) && IConstant.Common.NOT_UNIQUE.equals(checkPhoneUnique(user))) {
            throw new ThinkerException(CodeMSG.PhoneRepeat);
        }
        if (!StringHelper.isBlank(user.email) && IConstant.Common.NOT_UNIQUE.equals(checkEmailUnique(user))) {
            throw new ThinkerException(CodeMSG.EmailRepeat);
        }
    }

    private String checkEmailUnique(SysUserRequest user) {
        SysUser sysUser = sysUserDAO.checkEmailUnique(user.email);
        if (Objects.nonNull(sysUser) && sysUser.getId() != MathHelper.nvl(user.id)) {
            return IConstant.Common.NOT_UNIQUE;
        }
        return IConstant.Common.UNIQUE;
    }

    private String checkPhoneUnique(SysUserRequest user) {
        SysUser sysUser = sysUserDAO.checkPhoneUnique(user.phoneNumber);
        if (Objects.nonNull(sysUser) && sysUser.getId() != MathHelper.nvl(user.id)) {
            return IConstant.Common.NOT_UNIQUE;
        }
        return IConstant.Common.UNIQUE;
    }

    private void insertUserRole(SysUserRequest user, Long userId) {
        List<Long> roleIds = user.roleIds;
        if (!CollectsHelper.isNullOrEmpty(roleIds)) {
            // 新增用户与岗位管理
            List<SysUserRole> list = Lists.newArrayList();
            for (Long roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.userId = userId;
                ur.roleId = roleId;
                list.add(ur);
            }
            if (!CollectsHelper.isNullOrEmpty(list)) {
                userRoleDAO.batchUserRole(list);
            }
        }
    }

    private void insertUserPost(SysUserRequest user, Long userId) {
        List<Long> postIds = user.postIds;
        if (!CollectsHelper.isNullOrEmpty(postIds)) {
            // 新增用户与岗位管理
            List<SysUserPost> list = Lists.newArrayList();
            for (Long postId : postIds) {
                SysUserPost up = new SysUserPost();
                up.userId = userId;
                up.postId = postId;
                list.add(up);
            }
            if (!CollectsHelper.isNullOrEmpty(list)) {
                userPostDAO.batchUserPost(list);
            }
        }
    }

    public Map<String, Object> getInfo(Long userId) {
        HashMap<String, Object> result = Maps.newHashMap();
        List<SysRole> roles = roleService.selectRoleAll();
        result.put("roles", SysUser.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        result.put("posts", postService.selectPostAll());

        if (MathHelper.nvl(userId) > 0) {
            result.put("data", sysUserDAO.getSysUserById(userId));
            result.put("postIds", postService.selectIdsListByUserId(userId));
            result.put("roleIds", roleService.selectRoleIdListByUserId(userId));
        }
        return result;
    }

    private SysUser selectUserById(Long userId) {
        return sysUserDAO.selectUserById(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer update(SysUserRequest user) {
        SysUser sysUser = BeanHelper.castTo(user, SysUser.class);
        checkUserAllowed(sysUser);
        checkEmailAndPhone(user);
        sysUser.updatedBy = RequestContext.get().getSession().ext;
        Long userId = sysUser.getId();
        // 删除用户与角色关联
        userRoleDAO.deleteUserRoleByUserId(userId);
        // 新增用户与角色管理
        insertUserRole(user, userId);
        // 删除用户与岗位关联
        userPostDAO.deleteUserPostByUserId(userId);
        // 新增用户岗位关联
        insertUserPost(user, userId);

        return sysUserDAO.updateUser(sysUser);
    }

    private void checkUserAllowed(SysUser sysUser) {
        if (sysUser.isAdmin()) {
            throw new ThinkerException(CodeMSG.AdminNo);
        }
    }

    public Integer del(Long[] userIds) {
        for (Long userId : userIds) {
            checkUserAllowed(new SysUser(userId));
        }
        // 删除用户与角色关联
        userRoleDAO.deleteUserRole(userIds);
        // 删除用户与岗位关联
        userPostDAO.deleteUserPost(userIds);
        //删除用户信息
        return sysUserDAO.deleteUserByIds(userIds);
    }

    public Integer resetUserPwd(SysUserRequest user) {
        SysUser sysUser = BeanHelper.castTo(user, SysUser.class);
        checkUserAllowed(sysUser);
        sysUser.updatedBy = RequestContext.get().getSession().ext;
        sysUser.userPassword = LoginUtil.adminPwd(sysUser.userPassword);
        return sysUserDAO.updateUser(sysUser);
    }

    public Integer changeStatus(SysUserRequest user) {
        SysUser sysUser = BeanHelper.castTo(user, SysUser.class);
        checkUserAllowed(sysUser);
        sysUser.updatedBy = RequestContext.get().getSession().ext;
        return sysUserDAO.updateUser(sysUser);
    }

    public MyProfileResponse getProfile() {
        MyProfileResponse myProfileResponse = new MyProfileResponse();
        SysUser sysUser = selectUserById(Long.parseLong(RequestContext.get().getSession().uid));
        myProfileResponse.user = BeanHelper.castTo(sysUser, UserDTO.class);
        myProfileResponse.roleGroup = selectUserRoleGroup(sysUser);
        myProfileResponse.postGroup = selectUserPostGroup(sysUser);
        return myProfileResponse;
    }

    private String selectUserRoleGroup(SysUser sysUser) {
        List<SysRole> roles = roleService.getSysRoles(sysUser.getId());
        if (!CollectsHelper.isNullOrEmpty(roles)) {
            StringBuilder str = new StringBuilder();
            for (SysRole role : roles) {
                str.append(",").append(role.roleName);
            }
            if (!StringHelper.isBlank(str.toString())) {
                return str.substring(1);
            }
        }
        return StringHelper.EMPTY;
    }

    private String selectUserPostGroup(SysUser sysUser) {
        List<SysPost> posts = postService.selectPostListByUserId(sysUser.getId());
        if (!CollectsHelper.isNullOrEmpty(posts)) {
            StringBuilder str = new StringBuilder();
            for (SysPost post : posts) {
                str.append(",").append(post.postName);
            }
            if (!StringHelper.isBlank(str.toString())) {
                return str.substring(1);
            }
        }
        return StringHelper.EMPTY;
    }

    public Integer updateProfile(SysUserRequest user) {
        SysUser sysUser = BeanHelper.castTo(user, SysUser.class);
        int row = sysUserDAO.updateUser(sysUser);
        if (row > 0) {
            RequestContext.get().getSession().ext = sysUser.userName;
        }
        return row;
    }

    public Integer updateProfilePwd(String oldPassword, String newPassword) {
        SysUser sysUserById = sysUserDAO.getSysUserById(Long.parseLong(RequestContext.get().getSession().uid));
        String userPassword = sysUserById.userPassword;
        if (!matchesPassword(oldPassword, userPassword)) {
            throw new ThinkerException(CodeMSG.OldPasswordError);
        }
        if (matchesPassword(newPassword, userPassword)) {
            throw new ThinkerException(CodeMSG.newPasswordError);
        }
        return sysUserDAO.resetUserPwd(LoginUtil.adminPwd(newPassword), sysUserById.getId());
    }

    private boolean matchesPassword(String rawPassword, String userPassword) {
        return LoginUtil.adminPwd(rawPassword).equals(userPassword);
    }

    public String avatar(MultipartFile file, String fileName) {
        if (!file.isEmpty()) {
            String avatar = FileUploadUtils.upload(IConstant.Common.AVAtAR_PATH, file, fileName);
            int row = sysUserDAO.updateUserAvatar(avatar, RequestContext.get().getSession().uid);
            if (row > 0) {
                return avatar;
            }
        }
        throw new ThinkerException(CodeMSG.UploadImages);
    }
}
