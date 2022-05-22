package com.summer.manage.controller.system;

import com.google.common.collect.ImmutableMap;
import com.summer.common.core.BaseRest;
import com.summer.common.core.RpcReply;
import com.summer.common.view.parser.ApiOperation;
import com.summer.manage.dto.request.LoginRequest;
import com.summer.manage.dto.response.CaptchaResponse;
import com.summer.manage.dto.response.RouterResponse;
import com.summer.manage.dto.response.UserInfoResponse;
import com.summer.manage.service.system.LoginService;
import com.summer.manage.service.system.MenuService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.List;

/**
 * @Description：用户登陆
 * @Author：sacher
 * @Create：2020/10/20 5:09 下午
 **/
@RestController
@RequestMapping("/login")
public class LoginController extends BaseRest {
    @Inject
    private LoginService loginService;

    @Inject
    private MenuService menuService;

    @GetMapping("/getCode")
    @ApiOperation(name = "获取验证码")
    RpcReply<CaptchaResponse> getCode() {
        return RpcReply.onOk(loginService.getCode());
    }

    @PostMapping("/doLogin")
    @ApiOperation(name = "登陆授权")
    RpcReply<ImmutableMap> login(@Valid @RequestBody LoginRequest request) {
        return RpcReply.onOk(loginService.login(request));
    }

    @GetMapping("/getUserInfo")
    @ApiOperation(name = "获取用户信息")
    RpcReply<UserInfoResponse> getUserInfo() {
        return RpcReply.onOk(loginService.getUserInfo());
    }

    @GetMapping("/getRouters")
    @ApiOperation(name = "获取路由")
    RpcReply<List<RouterResponse>> getRouters() {
        return RpcReply.onOk(menuService.getRouters());
    }

    @PostMapping("/logout")
    @ApiOperation(name = "退出登陆")
    RpcReply<Integer> logout() {
        return RpcReply.onOk(loginService.logout());
    }

}
