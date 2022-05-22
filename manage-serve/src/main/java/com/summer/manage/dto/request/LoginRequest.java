package com.summer.manage.dto.request;


import com.summer.common.core.BaseDTO;
import com.summer.common.core.IRequest;

import javax.validation.constraints.NotBlank;

/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/27 9:35 下午
 **/
public class LoginRequest extends BaseDTO implements IRequest {
    @NotBlank
    public String userName;

    @NotBlank
    public String userPassword;

    public String code;

    public String uuid = "";

    @Override
    public void verify() {

    }
}
