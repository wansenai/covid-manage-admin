package com.summer.manage.controller.system;

import com.summer.common.core.BaseRest;
import com.summer.common.core.RpcReply;
import com.summer.common.view.parser.ApiOperation;
import com.summer.manage.dto.request.MenuRequest;
import com.summer.manage.dto.response.MenuResponse;
import com.summer.manage.dto.response.TreeSelectResponse;
import com.summer.manage.service.system.MenuService;
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
 * @Create：2021/1/8 11:51 下午
 **/
@RestController
@RequestMapping("/menu")
public class MenuController extends BaseRest {

    @Inject
    private MenuService menuService;

    @PostMapping("/listMenu")
    @ApiOperation(name = "获取菜单列表")
    RpcReply<List<MenuResponse>> listMenu(@RequestBody MenuRequest request) {
        return RpcReply.onOk(menuService.listMenu(request));
    }

    /**
     * 新增菜单
     */
    @PostMapping("/add")
    @ApiOperation(name = "新增菜单")
    public RpcReply<Integer> add(@Validated @RequestBody MenuRequest menu) {
        return RpcReply.onOk(menuService.add(menu));
    }

    /**
     * 修改菜单
     */
    @PutMapping("/edit")
    @ApiOperation(name = "修改菜单")
    public RpcReply<Integer> edit(@Validated @RequestBody MenuRequest menu) {
        return RpcReply.onOk(menuService.edit(menu));
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/remove/{menuId}")
    @ApiOperation(name = "删除菜单")
    public RpcReply<Integer> remove(@PathVariable("menuId") Long menuId) {
        return RpcReply.onOk(menuService.remove(menuId));
    }

    /**
     * 根据Id查询菜单
     */
    @GetMapping("/getMenu/{menuId}")
    @ApiOperation(name = "根据Id查询菜单")
    public RpcReply<MenuResponse> getMenu(@PathVariable("menuId") Long menuId) {
        return RpcReply.onOk(menuService.getMenu(menuId));
    }

    @GetMapping("/treeSelect")
    @ApiOperation(name = "菜单下拉树结构")
    public RpcReply<List<TreeSelectResponse>> treeSelect(MenuRequest request) {
        return RpcReply.onOk(menuService.treeSelect(request));
    }

    @GetMapping("/roleMenuTreeSelect/{id}")
    @ApiOperation(name = "根据Id查询菜单")
    public RpcReply<Map<String, Object>> roleMenuTreeSelect(@PathVariable("id") Long id) {
        return RpcReply.onOk(menuService.roleMenuTreeSelect(id));
    }
}
