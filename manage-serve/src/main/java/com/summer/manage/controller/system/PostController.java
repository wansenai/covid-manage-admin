package com.summer.manage.controller.system;

import com.summer.common.core.RpcReply;
import com.summer.common.support.Pagination;
import com.summer.common.view.parser.ApiOperation;
import com.summer.manage.dto.request.PostListRequest;
import com.summer.manage.dto.request.PostRequest;
import com.summer.manage.dto.response.SysPostResponse;
import com.summer.manage.service.system.PostService;
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
 * @Create：2021/2/10 10:38 AM
 **/
@RequestMapping("/post")
@RestController
public class PostController {
    @Inject
    private PostService postService;

    @PostMapping("/listPost")
    @ApiOperation(name = "获取岗位列表")
    RpcReply<Pagination<SysPostResponse>> listPost(@RequestBody PostListRequest request) {
        return RpcReply.onOk(postService.listPost(request));
    }

    @GetMapping("/getInfo/{id}")
    @ApiOperation(name = "岗位查询")
    RpcReply<SysPostResponse> getInfo(@PathVariable Long id) {
        return RpcReply.onOk(postService.getInfo(id));
    }

    @PostMapping("/add")
    @ApiOperation(name = "岗位新增")
    RpcReply<Integer> add(@Validated @RequestBody PostRequest request) {
        return RpcReply.onOk(postService.add(request));
    }

    @PutMapping("/update")
    @ApiOperation(name = "岗位修改")
    RpcReply<Integer> update(@Validated @RequestBody PostRequest request) {
        return RpcReply.onOk(postService.update(request));
    }

    @DeleteMapping("/del/{postIds}")
    @ApiOperation(name = "岗位删除")
    RpcReply<Integer> del(@PathVariable Long[] postIds) {
        return RpcReply.onOk(postService.del(postIds));
    }
}
