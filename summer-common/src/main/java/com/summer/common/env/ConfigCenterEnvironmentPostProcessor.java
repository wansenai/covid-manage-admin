package com.summer.common.env;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * 远程配置数据加载
 *
 */
@Configuration
public class ConfigCenterEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String COMMON_CONFIG_NAME   = "common-config";
    private static final String METADATA_CONF        = "eureka.instance.metadata-map.conf";
    private static final String MULTI_CONF_SEPARATOR = ",";

    private static final Logger log = LoggerFactory.getLogger(ConfigCenterEnvironmentPostProcessor.class);

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // 配置url
        String url = environment.getProperty("thinker.config-center.addr");
        if (StringUtils.isEmpty(url)) {
            log.info("未配置远程配置中心地址");
        } else {
            try (InputStream is = new URL(url).openStream()) {
                // 加载数据
                Properties properties = new Properties();
                properties.load(is);

                log.info("共加载{}个远程配置", properties.size());

                // 写入环境变量
                Map<String, Object> configMap = new LinkedHashMap<>();
                properties.forEach((k, v) -> configMap.put((String) k, v));
                PropertySource propertySource = new MapPropertySource(url, configMap);

                // 添加到最前面，最高优先级
                environment.getPropertySources().addFirst(propertySource);

                // 添加和记录当前启用的配置
                environment.addActiveProfile(url);
            } catch (IOException e) {
                log.error("加载远程配置失败: ", e);
            }
        }

        // eureka metadata配置
        addEurekaMetadata(environment);
    }

    private void addEurekaMetadata(ConfigurableEnvironment environment) {
        Properties properties = new Properties();

        String[]     profiles       = environment.getActiveProfiles();
        List<String> activeProfiles = new ArrayList<>(Arrays.asList(profiles));

        String conf = environment.getProperty(METADATA_CONF);
        if (!StringUtils.isEmpty(conf)) {
            String[] confArrays = conf.split(MULTI_CONF_SEPARATOR);
            activeProfiles.addAll(Arrays.asList(confArrays));
        }
        // 已配置的取出来去重合并再写入配置中
        activeProfiles = activeProfiles.parallelStream().distinct().collect(Collectors.toList());
        properties.put(METADATA_CONF, String.join(MULTI_CONF_SEPARATOR, activeProfiles));

        // 写入环境变量
        Map<String, Object> configMap = new LinkedHashMap<>();
        properties.forEach((k, v) -> configMap.put((String) k, v));
        PropertySource propertySource = new MapPropertySource(COMMON_CONFIG_NAME, configMap);

        // 添加到最前面，最高优先级
        environment.getPropertySources().addFirst(propertySource);
    }
}
