package com.summer.manage.controller.system;

import com.summer.common.core.BaseRest;
import com.summer.common.core.RpcReply;
import com.summer.common.support.Pagination;
import com.summer.common.view.parser.ApiOperation;
import com.summer.manage.dto.request.SysUserRequest;
import com.summer.manage.dto.request.UserListRequest;
import com.summer.manage.dto.response.MyProfileResponse;
import com.summer.manage.dto.response.UserResponse;
import com.summer.manage.service.system.UserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import java.util.Map;

/**
 * @Description：
 * @Author：sacher
 * @Create：2021/1/6 6:55 下午
 **/
@RestController
@RequestMapping("/user")
public class UserController extends BaseRest {

    @Inject
    private UserService userService;

    @PostMapping("/listUser")
    @ApiOperation(name = "获取用户列表")
    RpcReply<Pagination<UserResponse>> listUser(@RequestBody UserListRequest request) {
        return RpcReply.onOk(userService.listUser(request));
    }

    @PostMapping("/add")
    @ApiOperation(name = "用户新增")
    RpcReply<Integer> add(@Validated @RequestBody SysUserRequest user) {
        return RpcReply.onOk(userService.add(user));
    }

    @PutMapping("/update")
    @ApiOperation(name = "用户修改")
    RpcReply<Integer> update(@Validated @RequestBody SysUserRequest user) {
        return RpcReply.onOk(userService.update(user));
    }

    @GetMapping("/getInfo/{userId}")
    @ApiOperation(name = "用户查询")
    RpcReply<Map<String, Object>> getInfo(@PathVariable(value = "userId", required = false) Long userId) {
        return RpcReply.onOk(userService.getInfo(userId));
    }

    @DeleteMapping("/del/{userIds}")
    @ApiOperation(name = "用户删除")
    RpcReply<Integer> del(@PathVariable Long[] userIds) {
        return RpcReply.onOk(userService.del(userIds));
    }

    @PutMapping("/resetPwd")
    @ApiOperation(name = "重置密码")
    RpcReply<Integer> resetUserPwd(@RequestBody SysUserRequest user) {
        return RpcReply.onOk(userService.resetUserPwd(user));
    }

    @PutMapping("/changeStatus")
    @ApiOperation(name = "状态修改")
    RpcReply<Integer> changeStatus(@RequestBody SysUserRequest user) {
        return RpcReply.onOk(userService.changeStatus(user));
    }

    @GetMapping("/getProfile")
    @ApiOperation(name = "个人信息查询")
    RpcReply<MyProfileResponse> getProfile() {
        return RpcReply.onOk(userService.getProfile());
    }


    @PutMapping("/updateProfile")
    @ApiOperation(name = "个人信息修改")
    RpcReply<Integer> updateProfile(@RequestBody SysUserRequest user) {
        return RpcReply.onOk(userService.updateProfile(user));
    }

    @PutMapping("/updateProfilePwd")
    @ApiOperation(name = "个人信息密码修改")
    RpcReply<Integer> updateProfilePwd(String oldPassword, String newPassword) {
        return RpcReply.onOk(userService.updateProfilePwd(oldPassword, newPassword));
    }

    @PostMapping("/avatar")
    @ApiOperation(name = "用户头像信息上传")
    RpcReply<String> avatar(@RequestParam("avatarFile") MultipartFile file, @RequestParam("fileName") String fileName) {
        return RpcReply.onOk(userService.avatar(file, fileName));
    }

}
