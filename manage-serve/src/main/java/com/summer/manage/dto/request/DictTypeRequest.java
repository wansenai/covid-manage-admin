package com.summer.manage.dto.request;


import com.summer.common.core.BaseDTO;
import com.summer.common.core.IRequest;

/**
 * @Description：
 * @Author：sacher
 * @Create：2021/1/9 9:49 下午
 **/
public class DictTypeRequest extends BaseDTO implements IRequest {
    public Long id;
    /**
     * 字典名称
     */
    public String dictName;

    /**
     * 字典类型
     */
    public String dictType;

    /**
     * 状态（0正常 1停用）
     */
    public String status;

    public String remark;

    @Override
    public void verify() {

    }
}
