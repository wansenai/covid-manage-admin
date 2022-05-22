package com.summer.manage.service.system;


import com.summer.common.core.BaseService;
import com.summer.common.core.ExpireKey;
import com.summer.common.exception.ThinkerException;
import com.summer.common.helper.BeanHelper;
import com.summer.common.helper.MathHelper;
import com.summer.common.helper.StringHelper;
import com.summer.common.redis.RedisFactory;
import com.summer.common.redis.RedisOperations;
import com.summer.common.support.Pagination;
import com.summer.common.view.parser.RequestContext;
import com.summer.manage.config.IRedis;
import com.summer.manage.core.StringUtil;
import com.summer.manage.dao.system.ConfigDAO;
import com.summer.manage.dto.request.ConfigListRequest;
import com.summer.manage.dto.request.ConfigRequest;
import com.summer.manage.dto.response.ConfigResponse;
import com.summer.manage.entity.system.SysConfig;
import com.summer.manage.kern.CodeMSG;
import com.summer.manage.kern.IConstant;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Set;


/**
 * @Description：
 * @Author：sacher
 * @Create：2021/1/6 3:05 下午
 **/
@Service
public class ConfigService extends BaseService {

    @Inject
    private ConfigDAO configDAO;

    private RedisOperations redisOperations() {
        return RedisFactory.get(IRedis.DEFAULT);
    }

    public String getConfigKey(String configKey) {
        String conf = redisOperations().one(IConstant.Redis.SYS_CONFIG_KEY + configKey, String.class);
        if (!StringHelper.isBlank(conf)) {
            return conf;
        }
        String newConf = configDAO.selectConfigValue(configKey);
        if (!StringHelper.isBlank(newConf)) {
            redisOperations().put(IConstant.Redis.SYS_CONFIG_KEY + configKey, newConf, ExpireKey.Forever.expire);
            return newConf;
        }
        return StringHelper.EMPTY;
    }

    public Pagination<ConfigResponse> listConfig(ConfigListRequest request) {
        Pagination<ConfigResponse> pagination = Pagination.create(request.pager, request.size);

        List<SysConfig> sysConfigs = configDAO.listConfig(request.configName,
                                                          request.configKey,
                                                          request.configType,
                                                          request.beginTime,
                                                          request.endTime,
                                                          pagination.getOffset(),
                                                          pagination.getSize());

        pagination.getList().addAll(BeanHelper.castTo(sysConfigs, ConfigResponse.class));
        Long total = configDAO.listConfigCount(request.configName,
                                               request.configKey,
                                               request.configType,
                                               request.beginTime,
                                               request.endTime);
        pagination.setTotal(null == total ? 0L : total);
        return pagination;
    }

    public ConfigResponse getInfo(Long id) {
        return BeanHelper.castTo(configDAO.selectById(id), ConfigResponse.class);
    }

    public Integer add(ConfigRequest request) {
        if (IConstant.Common.NOT_UNIQUE.equals(checkConfigKeyUnique(request))) {
            throw new ThinkerException(CodeMSG.ConfigKeyRepeat);
        }
        SysConfig sysConfig = BeanHelper.castTo(request, SysConfig.class);
        sysConfig.createdBy = RequestContext.get().getSession().ext;
        return insertConfig(sysConfig);
    }

    private Integer insertConfig(SysConfig sysConfig) {
        int row = configDAO.insert(sysConfig);
        if (row > 0) {
            redisOperations().put(IConstant.Redis.SYS_CONFIG_KEY + sysConfig.configKey, sysConfig.configValue, ExpireKey.Forever.expire);
        }
        return row;
    }

    private String checkConfigKeyUnique(ConfigRequest request) {
        SysConfig sysConfig = configDAO.checkConfigKeyUnique(request.configKey);
        if (Objects.nonNull(sysConfig) && sysConfig.getId() != MathHelper.nvl(request.id)) {
            return IConstant.Common.NOT_UNIQUE;
        }
        return IConstant.Common.UNIQUE;
    }

    public Integer update(ConfigRequest request) {
        if (IConstant.Common.NOT_UNIQUE.equals(checkConfigKeyUnique(request))) {
            throw new ThinkerException(CodeMSG.ConfigKeyRepeat);
        }
        SysConfig sysConfig = BeanHelper.castTo(request, SysConfig.class);
        sysConfig.updatedBy = RequestContext.get().getSession().ext;
        return updateConfig(sysConfig);
    }

    private Integer updateConfig(SysConfig sysConfig) {
        int row = configDAO.update(sysConfig);
        if (row > 0) {
            redisOperations().put(IConstant.Redis.SYS_CONFIG_KEY + sysConfig.configKey, sysConfig.configValue, ExpireKey.Forever.expire);
        }
        return row;
    }

    public Integer del(Long[] ids) {
        for (Long configId : ids) {
            SysConfig config = configDAO.selectById(configId);
            if (StringUtil.equals(IConstant.UserMenu.YES, config.configType)) {
                throw new ThinkerException(CodeMSG.Common.code(), config.configName + "内置参数，不能删除");
            }
        }
        int count = configDAO.deleteConfigByIds(ids);
        if (count > 0) {
            clearConfigCache();
        }
        return count;
    }

    public int clearConfigCache() {
        int count = 0;
        Set<String> strings = redisOperations().keysGet(IConstant.Redis.SYS_CONFIG_KEY + "*");
        for (String string : strings) {
            count += redisOperations().clear(string);
        }
        return count;
    }
}
