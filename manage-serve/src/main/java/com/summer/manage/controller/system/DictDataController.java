package com.summer.manage.controller.system;

import com.summer.common.core.BaseRest;
import com.summer.common.core.RpcReply;
import com.summer.common.support.Pagination;
import com.summer.common.view.parser.ApiOperation;
import com.summer.manage.dto.request.DictDataListRequest;
import com.summer.manage.dto.request.DictDataRequest;
import com.summer.manage.dto.response.DictDataResponse;
import com.summer.manage.service.system.DictDataService;
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

/**
 * @Description：
 * @Author：sacher
 * @Create：2021/1/8 2:34 下午
 **/
@RestController
@RequestMapping("/dict/data")
public class DictDataController extends BaseRest {

    @Inject
    private DictDataService dictDataService;

    @GetMapping("/getDataByType/{dictType}")
    @ApiOperation(name = "字典类型查询字典数据")
    RpcReply<List<DictDataResponse>> getDataByType(@PathVariable String dictType) {
        return RpcReply.onOk(dictDataService.getDataByType(dictType));
    }

    @PostMapping("/listData")
    @ApiOperation(name = "获取字典数据")
    RpcReply<Pagination<DictDataResponse>> listData(@RequestBody DictDataListRequest request) {
        return RpcReply.onOk(dictDataService.listData(request));
    }

    @PostMapping("/add")
    @ApiOperation(name = "新增字典数据")
    RpcReply<Integer> add(@RequestBody DictDataRequest request) {
        return RpcReply.onOk(dictDataService.add(request));
    }

    @PutMapping("/update")
    @ApiOperation(name = "修改字典数据")
    RpcReply<Integer> update(@RequestBody DictDataRequest request) {
        return RpcReply.onOk(dictDataService.update(request));
    }

    @DeleteMapping("/del/{ids}")
    @ApiOperation(name = "删除字典数据")
    RpcReply<Integer> del(@PathVariable Long[] ids) {
        return RpcReply.onOk(dictDataService.del(ids));
    }

    @GetMapping("/getData/{id}")
    @ApiOperation(name = "查询字典数据")
    RpcReply<DictDataResponse> getData(@PathVariable Long id) {
        return RpcReply.onOk(dictDataService.getData(id));
    }
}
