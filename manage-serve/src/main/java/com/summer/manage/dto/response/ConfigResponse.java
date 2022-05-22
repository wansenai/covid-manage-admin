package com.summer.manage.dto.response;

import java.util.Date;

/**
 * @Description：
 * @Author：sacher
 * @Create：2021/1/9 9:47 下午
 **/
public class ConfigResponse {

    public Long id;

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


    /**
     * 创建时间
     **/
    public Date createdAt;
    /**
     * 更新时间
     **/
    public Date updatedAt;

    public String createdBy;

    public String updatedBy;
}
