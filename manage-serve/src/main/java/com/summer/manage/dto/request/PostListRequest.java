package com.summer.manage.dto.request;


import com.summer.common.core.BaseDTO;
import com.summer.common.core.IRequest;

/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/30 10:42 上午
 **/
public class PostListRequest extends BaseDTO implements IRequest {

    /**
     * 当前页
     **/
    public Integer pager = 1;

    /**
     * 每页条数
     **/
    public Integer size = 20;

    public String postCode;

    public String postName;

    public String status;

    @Override
    public void verify() {

    }
}
