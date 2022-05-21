package com.summer.manage.config;

import com.summer.common.redis.RedisFactory;
import com.summer.common.redis.RedisOperations;
import com.summer.common.view.IWebAuthenticationFilter;
import com.summer.common.view.parser.RequestSession;
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

    private RedisOperations redisOperations() {
        return RedisFactory.get(IRedis.DEFAULT);
    }

    WebAuthenticationFilter(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public boolean authentication(RequestSession session, HttpServletRequest request, HttpServletResponse response) {
        String signature = session.signature;

        return true;
    }
}
