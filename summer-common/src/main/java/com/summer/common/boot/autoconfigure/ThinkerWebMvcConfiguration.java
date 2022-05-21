package com.summer.common.boot.autoconfigure;

import com.summer.common.filter.RequestArgumentValidateFilter;
import com.summer.common.filter.ResponseBodyFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自定义webMvc自动配置
 *
 */
@Configuration
public class ThinkerWebMvcConfiguration {

    /**
     * 自动校验请求参数
     */
    @Bean
    @ConditionalOnProperty("thinker.request.validate.enabled")
    public RequestArgumentValidateFilter requestArgumentValidateFilter() {
        return new RequestArgumentValidateFilter();
    }

    /**
     * 自动包装响应结构体
     */
    @Bean
    @ConditionalOnProperty("thinker.response.simplefy.enabled")
    public ResponseBodyFilter responseBodyHandler() {
        return new ResponseBodyFilter();
    }
}
