package com.summer.common.redis;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.summer.common.helper.BytesHelper;
import com.summer.common.helper.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisShardInfo;

import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Set;

public final class RedisFactory {
    static final Map<String, RedisOperations> REDIS_MAP = Maps.newConcurrentMap();

    RedisFactory() {
    }

    public static RedisOperations get(String dynamicRedis) {
        return REDIS_MAP.get(dynamicRedis);
    }

    static ShardInfo shardInfo(String host, int port, String name, boolean ssl, int db, String password) {
        return new ShardInfo(host, port, name, ssl, db, password);
    }

    static class ShardInfo extends JedisShardInfo {
        ShardInfo(String host, int port, String name, boolean ssl, int db, String password) {
            super(host, port, name, ssl);
            try {
                Field field = this.getClass().getSuperclass().getDeclaredField("db");
                field.setAccessible(true);
                field.set(this, db);
                field.setAccessible(false);
            } catch (Exception e) {
                throw new RedisException("Redis set db=" + db + "error...", e);
            }
            if (!StringHelper.isBlank(password)) {
                super.setPassword(password);
            }
        }

        ShardInfo ofTimeout(int timeout) {
            if (timeout > 0) {
                super.setSoTimeout(timeout);
                super.setConnectionTimeout(timeout);
            }
            return this;
        }
    }

    static class UriInfo {
        private static final Logger LOG = LoggerFactory.getLogger(UriInfo.class);
        final int dbIdx;
        final boolean ssl;
        final String name;
        final String password;
        final Set<HostAndPort> hapSet = Sets.newHashSet();

        // URI=REDIS_URI={SCHEME}://{NAME}:{PWD}@{HOST:PORT},{HOST:PORT}/{DB}
        UriInfo(String uri) {
            LOG.info("redis uri: {}", uri);
            if (StringHelper.isBlank(uri)) {
                throw new RedisException("Redis uri must not null/empty.....");
            }
            try {
                this.ssl = uri.contains("s://");
                int pathIdx = uri.lastIndexOf("/");
                this.dbIdx = Integer.parseInt(uri.substring(pathIdx + 1));
                String[] locations = uri.substring(uri.indexOf("@") + 1, pathIdx).split(",");
                for (String location : locations) {
                    hapSet.add(HostAndPort.parseString(location));
                }
                String[] uis = StringHelper.substringBetween(uri, "://", "@").split(":");
                if (uis.length < 1) {
                    this.name = this.password = null;
                } else {
                    this.name = StringHelper.defaultIfBlank(URLDecoder.decode(uis[0], BytesHelper.UTF8.name()), null);
                    this.password = StringHelper.defaultIfBlank(URLDecoder.decode(uis[1], BytesHelper.UTF8.name()), null);
                }
            } catch (Exception e) {
                throw new RedisException("Redis uri must format with {SCHEME}://{NAME}:{PWD}@{HOST:PORT},{HOST:PORT}/{DB} ");
            }
        }
    }
}
