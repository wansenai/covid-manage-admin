package com.summer.manage.controller.system;

import com.summer.common.core.BaseRest;
import com.summer.common.core.RpcReply;
import com.summer.common.support.Pagination;
import com.summer.common.view.parser.ApiOperation;
import com.summer.manage.dto.request.ConfigListRequest;
import com.summer.manage.dto.request.ConfigRequest;
import com.summer.manage.dto.response.ConfigResponse;
import com.summer.manage.service.system.ConfigService;
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
@RequestMapping("/config")
public class ConfigController extends BaseRest {

    @Inject
    private ConfigService configService;

    @GetMapping("/getConfigKey/{configKey}")
    @ApiOperation(name = "根据Key查询参数名")
    RpcReply<String> getConfigKey(@PathVariable String configKey) {
        return RpcReply.onOk(configService.getConfigKey(configKey));
    }

    @PostMapping("/listConfig")
    @ApiOperation(name = "获取参数列表")
    RpcReply<Pagination<ConfigResponse>> listConfig(@RequestBody ConfigListRequest request) {
        return RpcReply.onOk(configService.listConfig(request));
    }

    @GetMapping("/getInfo/{id}")
    @ApiOperation(name = "根据ID查询参数")
    RpcReply<ConfigResponse> getInfo(@PathVariable Long id) {
        return RpcReply.onOk(configService.getInfo(id));
    }

    @PostMapping("/add")
    @ApiOperation(name = "参数新增")
    RpcReply<Integer> add(@Validated @RequestBody ConfigRequest request) {
        return RpcReply.onOk(configService.add(request));
    }

    @PutMapping("/update")
    @ApiOperation(name = "参数修改")
    RpcReply<Integer> update(@Validated @RequestBody ConfigRequest request) {
        return RpcReply.onOk(configService.update(request));
    }

    @DeleteMapping("/del/{ids}")
    @ApiOperation(name = "参数删除")
    RpcReply<Integer> del(@PathVariable Long[] ids) {
        return RpcReply.onOk(configService.del(ids));
    }

    @DeleteMapping(value = "/clearCache")
    @ApiOperation(name = "清理参数缓存")
    RpcReply<Integer> clearCache() {
        return RpcReply.onOk(configService.clearConfigCache());
    }
}
