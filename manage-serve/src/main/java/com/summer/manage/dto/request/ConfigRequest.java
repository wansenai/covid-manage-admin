package com.summer.manage.dto.request;


import com.summer.common.core.BaseDTO;
import com.summer.common.core.IRequest;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @Description：
 * @Author：sacher
 * @Create：2021/1/9 9:49 下午
 **/
public class ConfigRequest extends BaseDTO implements IRequest {

    public Long id;

    /**
     * 参数名称
     */
    @NotBlank(message = "参数名称不能为空")
    @Size(max = 100, message = "参数名称不能超过100个字符")
    public String configName;

    /**
     * 参数键名
     */
    @NotBlank(message = "参数键名长度不能为空")
    @Size(max = 100, message = "参数键名长度不能超过100个字符")
    public String configKey;

    /**
     * 参数键值
     */
    @NotBlank(message = "参数键值不能为空")
    @Size(max = 500, message = "参数键值长度不能超过500个字符")
    public String configValue;

    /**
     * 系统内置（Y是 N否）
     */
    public String configType;

    public String remark;

    @Override
    public void verify() {

    }
}
