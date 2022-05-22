package com.summer.manage.controller.system;

import com.summer.common.core.RpcReply;
import com.summer.common.support.Pagination;
import com.summer.common.view.parser.ApiOperation;
import com.summer.manage.dto.request.RoleListRequest;
import com.summer.manage.dto.request.RoleRequest;
import com.summer.manage.dto.response.SysRoleResponse;
import com.summer.manage.service.system.RoleService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * @Description：
 * @Author：sacher
 * @Create：2021/2/8 12:05 AM
 **/
@RestController
@RequestMapping("/role")
public class RoleController {
    @Inject
    private RoleService roleService;

    @PostMapping("/listRole")
    @ApiOperation(name = "获取用户列表")
    RpcReply<Pagination<SysRoleResponse>> listRole(@RequestBody RoleListRequest request) {
        return RpcReply.onOk(roleService.listRole(request));
    }

    @GetMapping("/getInfo/{id}")
    @ApiOperation(name = "用户查询")
    RpcReply<SysRoleResponse> getInfo(@PathVariable Long id) {
        return RpcReply.onOk(roleService.getInfo(id));
    }

    @PutMapping("/changeStatus")
    @ApiOperation(name = "状态修改")
    RpcReply<Integer> changeStatus(@RequestBody RoleRequest request) {
        return RpcReply.onOk(roleService.changeStatus(request));
    }

    @PostMapping("/add")
    @ApiOperation(name = "角色新增")
    RpcReply<Integer> add(@Validated @RequestBody RoleRequest request) {
        return RpcReply.onOk(roleService.add(request));
    }

    @PutMapping("/update")
    @ApiOperation(name = "角色修改")
    RpcReply<Integer> update(@Validated @RequestBody RoleRequest request) {
        return RpcReply.onOk(roleService.update(request));
    }

    @PutMapping("/dataScope")
    @ApiOperation(name = "数据权限修改")
    RpcReply<Integer> dataScope(@RequestBody RoleRequest request) {
        return RpcReply.onOk(roleService.dataScope(request));
    }

    @DeleteMapping("/del/{roleIds}")
    @ApiOperation(name = "角色删除")
    RpcReply<Integer> del(@PathVariable Long[] roleIds) {
        return RpcReply.onOk(roleService.del(roleIds));
    }

}
