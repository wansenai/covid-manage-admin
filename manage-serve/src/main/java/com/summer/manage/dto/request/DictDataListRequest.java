package com.summer.manage.dto.request;


import com.summer.common.core.BaseDTO;
import com.summer.common.core.IRequest;

/**
 * @Description：
 * @Author：sacher
 * @Create：2021/1/10 1:46 上午
 **/
public class DictDataListRequest extends BaseDTO implements IRequest {

    public Long id;

    /**
     * 当前页
     **/
    public Integer pager = 1;

    /**
     * 每页条数
     **/
    public Integer size = 20;

    public String dictLabel;

    public String dictType;

    public String status;


    @Override
    public void verify() {

    }
}
