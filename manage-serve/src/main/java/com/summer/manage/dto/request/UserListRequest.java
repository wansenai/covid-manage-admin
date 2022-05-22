package com.summer.manage.dto.request;


import com.summer.common.core.BaseDTO;
import com.summer.common.core.IRequest;
import com.summer.common.helper.StringHelper;

/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/30 10:42 上午
 **/
public class UserListRequest extends BaseDTO implements IRequest {

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

    public String phoneNumber;

    public String userName;

    public String status;
    public Long deptId;

    public String userName() {
        if (!StringHelper.isBlank(userName)) {
            return "%" + userName + "%";
        }
        return null;
    }

    public String phoneName() {
        if (!StringHelper.isBlank(phoneNumber)) {
            return "%" + phoneNumber + "%";
        }
        return null;
    }

    @Override
    public void verify() {

    }
}
