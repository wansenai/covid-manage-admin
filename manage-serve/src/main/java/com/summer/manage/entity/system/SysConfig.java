package com.summer.manage.entity.system;


import com.summer.common.core.BaseEntity;
import com.summer.common.ibatis.DefaultStrategy;

/**
 * @ClassName SysUser
 * @Description:
 * @Author Sacher
 * @Date 2020/11/26
 **/
public class SysConfig extends BaseEntity<DefaultStrategy> {
    /**
     * 参数名称
     */
    public String configName;

    /**
     * 参数键名
     */
    public String configKey;

    /**
     * 参数键值
     */
    public String configValue;

    /**
     * 系统内置（Y是 N否）
     */
    public String configType;

    public String remark;

}
