package com.summer.manage.controller.system;

import com.summer.common.core.BaseRest;
import com.summer.common.core.RpcReply;
import com.summer.common.view.parser.ApiOperation;
import com.summer.manage.dto.request.DeptRequest;
import com.summer.manage.dto.response.DeptResponse;
import com.summer.manage.dto.response.TreeSelectResponse;
import com.summer.manage.service.system.DeptService;
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
import java.util.List;
import java.util.Map;

/**
 * @Description：
 * @Author：sacher
 * @Create：2021/1/7 2:16 下午
 **/

@RestController
@RequestMapping("/dept")
public class DeptController extends BaseRest {

    @Inject
    private DeptService deptService;

    @PostMapping("/listDept")
    @ApiOperation(name = "获取部门列表")
    RpcReply<List<DeptResponse>> listDept(@RequestBody DeptRequest request) {
        return RpcReply.onOk(deptService.listDept(request));
    }

    @GetMapping("/getInfo/{deptId}")
    @ApiOperation(name = "部门查询")
    RpcReply<DeptResponse> getInfo(@PathVariable Long deptId) {
        return RpcReply.onOk(deptService.getInfo(deptId));
    }

    @GetMapping("/excludeList/{deptId}")
    @ApiOperation(name = "部门列表（排除节点）")
    RpcReply<List<DeptResponse>> excludeList(@PathVariable Long deptId) {
        return RpcReply.onOk(deptService.excludeList(deptId));
    }

    @GetMapping("/treeSelect")
    @ApiOperation(name = "部门下拉树列表")
    RpcReply<List<TreeSelectResponse>> treeSelect(DeptRequest deptRequest) {
        return RpcReply.onOk(deptService.buildDeptTreeSelect(deptRequest));
    }

    @GetMapping("/roleDeptTreeSelect/{id}")
    @ApiOperation(name = "角色ID查询部门树结构")
    RpcReply<Map<String, Object>> roleDeptTreeSelect(@PathVariable Long id) {
        return RpcReply.onOk(deptService.roleDeptTreeSelect(id));
    }

    @PostMapping("/add")
    @ApiOperation(name = "部门新增")
    RpcReply<Integer> add(@Validated @RequestBody DeptRequest request) {
        return RpcReply.onOk(deptService.add(request));
    }

    @PutMapping("/update")
    @ApiOperation(name = "部门修改")
    RpcReply<Integer> update(@Validated @RequestBody DeptRequest request) {
        return RpcReply.onOk(deptService.update(request));
    }

    @DeleteMapping("/del/{deptId}")
    @ApiOperation(name = "部门删除")
    RpcReply<Integer> del(@PathVariable Long deptId) {
        return RpcReply.onOk(deptService.del(deptId));
    }

}
