package com.summer.manage.dto.request;


import com.summer.common.core.BaseDTO;
import com.summer.common.core.IRequest;

/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/30 10:42 上午
 **/
public class ImportTableRequest extends BaseDTO implements IRequest {
    public String dbName;

    public String tables;

    @Override
    public void verify() {

    }
}
