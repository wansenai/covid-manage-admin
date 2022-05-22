package com.summer.manage.config;

import com.summer.common.core.ExpireKey;
import com.summer.common.core.RpcException;
import com.summer.common.helper.SpringHelper;
import com.summer.common.helper.StringHelper;
import com.summer.common.redis.RedisFactory;
import com.summer.common.redis.RedisOperations;
import com.summer.common.support.CommonCode;
import com.summer.common.view.IWebAuthenticationFilter;
import com.summer.common.view.parser.RequestSession;
import com.summer.manage.entity.system.SysUser;
import com.summer.manage.kern.IConstant;
import com.summer.manage.service.system.LoginService;
import com.summer.manage.service.system.PermissionService;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/20 9:40 AM
 **/
public class WebAuthenticationFilter implements IWebAuthenticationFilter {

    private final ApplicationContext context;
    private final LoginService loginService = SpringHelper.getBean(LoginService.class);
    private final PermissionService permissionService = SpringHelper.getBean(PermissionService.class);

    WebAuthenticationFilter(ApplicationContext context) {
        this.context = context;
    }

    private RedisOperations redisOperations() {
        return RedisFactory.get(IRedis.DEFAULT);
    }

    @Override
    public boolean authentication(RequestSession session, HttpServletRequest request, HttpServletResponse response) {
        String signature = session.signature;
        //不需要登陆的接口
        if (this.exclude(session.uri)) {
            return true;
        }

        //需要登陆
        if (StringHelper.isBlank(signature)) {
            throw new RpcException(CommonCode.InvalidToekn);
        }

        //验证token
        String one = redisOperations().one(IConstant.Redis.REDIS_LOGIN_USER + signature, String.class);
        if (StringHelper.isBlank(one)) {
            throw new RpcException(CommonCode.InvalidToekn);
        } else {
            //判断账号是否可以用
            SysUser sysUser = loginService.getUserById(Long.parseLong(one));
//            Set<String> menuPermission = permissionService.getMenuPermission(sysUser);
//            if(CollectsHelper.isNullOrEmpty(menuPermission)){
//                throw new RpcException(CommonCode.Forbidden);
//            }
//            boolean flag = false;
//            for (String s : menuPermission) {
//                String uri = session.uri;
//                if(uri.contains(s) || excludeMain(uri)){
//                    flag = true;
//                    break;
//                }
//            }
//            if (!flag) {
//                throw new RpcException(CommonCode.Forbidden);
//            }
            //设置时间
            redisOperations().put(IConstant.Redis.REDIS_LOGIN_USER + signature, one, ExpireKey.Days1.expire);
            session.uid = one;
            session.ext = sysUser.userName;
        }
        return true;
    }

    private boolean excludeMain(String uri) {
        if (uri.contains("/login/getUserInfo")) {
            return true;
        }
        if (uri.contains("/login/getRouters")) {
            return true;
        }
        return false;
    }

    private boolean exclude(String uri) {
        if (uri.contains("/login/getCode")) {
            return true;
        }
        if (uri.contains("/login/doLogin")) {
            return true;
        }
        if (uri.contains("/manage-serve/health_check")) {
            return true;
        }
        if (uri.contains("/file")) {
            return true;
        }
        return false;
    }


}
