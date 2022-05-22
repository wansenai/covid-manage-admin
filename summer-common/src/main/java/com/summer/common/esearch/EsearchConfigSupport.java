package com.summer.common.esearch;

import com.summer.common.helper.SpringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.util.Map;

public abstract class EsearchConfigSupport {
    private static final Logger LOG = LoggerFactory.getLogger(EsearchConfigSupport.class);

    protected EsearchConfigSupport() {
        EomInitializer.prepare(modelBasePackage());
        addMultiEsearchOperations(SpringHelper.getEnvironment(), EsearchFactory.ES_MAP);
        EomInitializer.processed();
    }

    /**
     * 如: com.thinker.tob
     **/
    protected abstract String modelBasePackage();

    /**
     * 添加多个 Elasticsearch 操作
     **/
    protected abstract void addMultiEsearchOperations(Environment env, Map<String, EsearchOperations> esMap);

    /**
     * ES://cluster@host:port,host:port,host:port
     */
    protected EsearchOperations newborn(String uri, boolean showQuery) {
        LOG.info("elasticsearch uri: {}", uri);
        return new EsearchOperations(EsearchFactory.transportClient(uri), showQuery);
    }

    @Bean
    public EsearchHealth esearchHealth() {
        return new EsearchHealth();
    }

    public static class EsearchHealth implements HealthIndicator {
        @Override
        public Health health() {
            Health.Builder builder = Health.up();
            for (Map.Entry<String, EsearchOperations> entry : EsearchFactory.ES_MAP.entrySet()) {
                builder.withDetail(entry.getKey(), entry.getValue().listNodes());
            }
            return builder.build();
        }
    }
}
