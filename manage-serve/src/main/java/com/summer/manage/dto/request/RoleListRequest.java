package com.summer.manage.dto.request;


import com.summer.common.core.BaseDTO;
import com.summer.common.core.IRequest;

/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/30 10:42 上午
 **/
public class RoleListRequest extends BaseDTO implements IRequest {

    /**
     * 当前页
     **/
    public Integer pager = 1;

    /**
     * 每页条数
     **/
    public Integer size = 20;

    /**
     * 开始时间
     */
    public String beginTime;

    /**
     * 结束时间
     */
    public String endTime;

    public String roleName;

    public String roleKey;

    public String status;

    @Override
    public void verify() {

    }
}
