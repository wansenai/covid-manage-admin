package com.summer.manage.dto.response;


import com.summer.common.core.BaseDTO;

import java.util.Set;

/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/28 11:23 上午
 **/
public class UserInfoResponse extends BaseDTO {
    public UserDTO user;
    public Set<String> roles;
    public Set<String> permissions;
}
