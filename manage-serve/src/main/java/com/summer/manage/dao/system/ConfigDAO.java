package com.summer.manage.dao.system;

import com.summer.manage.entity.system.SysConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/28 12:14 上午
 **/
public interface ConfigDAO {

    String selectConfigValue(@Param("configKey") String configKey);

    List<SysConfig> listConfig(@Param("configName") String configName, @Param("configKey") String configKey, @Param("configType") String configType, @Param("beginTime") String beginTime, @Param("endTime") String endTime, @Param("offset") int offset, @Param("size") int size);

    Long listConfigCount(@Param("configName") String configName, @Param("configKey") String configKey, @Param("configType") String configType, @Param("beginTime") String beginTime, @Param("endTime") String endTime);

    SysConfig selectById(@Param("id") Long id);

    SysConfig checkConfigKeyUnique(@Param("configKey") String configKey);

    int insert(SysConfig sysConfig);

    int update(SysConfig sysConfig);

    int deleteConfigByIds(Long[] configIds);
}
