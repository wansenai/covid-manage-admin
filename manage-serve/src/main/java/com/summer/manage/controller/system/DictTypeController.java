package com.summer.manage.controller.system;

import com.summer.common.core.BaseRest;
import com.summer.common.core.RpcReply;
import com.summer.common.support.Pagination;
import com.summer.common.view.parser.ApiOperation;
import com.summer.manage.dto.request.DictTypeListRequest;
import com.summer.manage.dto.request.DictTypeRequest;
import com.summer.manage.dto.response.DictTypeResponse;
import com.summer.manage.service.system.DictTypeService;
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
 * @Create：2021/1/8 2:34 下午
 **/
@RestController
@RequestMapping("/dict/type")
public class DictTypeController extends BaseRest {

    @Inject
    private DictTypeService dictTypeService;

    @PostMapping("/listType")
    @ApiOperation(name = "获取字典类型")
    RpcReply<Pagination<DictTypeResponse>> listType(@RequestBody DictTypeListRequest request) {
        return RpcReply.onOk(dictTypeService.listType(request));
    }

    @PostMapping("/add")
    @ApiOperation(name = "添加字典类型")
    RpcReply<Integer> add(@Validated @RequestBody DictTypeRequest request) {
        return RpcReply.onOk(dictTypeService.add(request));
    }

    @PutMapping("/update")
    @ApiOperation(name = "修改字典类型")
    RpcReply<Integer> update(@Validated @RequestBody DictTypeRequest request) {
        return RpcReply.onOk(dictTypeService.update(request));
    }

    /**
     * 查询字典类型详细
     */
    @GetMapping(value = "/getType/{id}")
    @ApiOperation(name = "查询字典类型")
    RpcReply<DictTypeResponse> getType(@PathVariable Long id) {
        return RpcReply.onOk(dictTypeService.selectDictTypeById(id));
    }

    /**
     * 删除字典类型
     */
    @DeleteMapping(value = "/del/{dictIds}")
    @ApiOperation(name = "查询字典类型")
    RpcReply<Integer> del(@PathVariable Long[] dictIds) {
        return RpcReply.onOk(dictTypeService.del(dictIds));
    }

    /**
     * 清理缓存
     */
    @DeleteMapping(value = "/clearCache")
    @ApiOperation(name = "清理字典缓存")
    RpcReply<Integer> clearCache() {
        return RpcReply.onOk(dictTypeService.clearDictCache());
    }

}
