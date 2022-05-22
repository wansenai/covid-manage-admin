package com.summer.manage.dto.request;


import com.summer.common.core.BaseDTO;
import com.summer.common.core.IRequest;

import java.util.Date;


/**
 * @Description：
 * @Author：sacher
 * @Create：2021/1/10 1:46 上午
 **/
public class DictDataRequest extends BaseDTO implements IRequest {

    public Long id;

    /**
     * 字典排序
     */
    public Long dictSort;

    /**
     * 字典标签
     */
    public String dictLabel;

    /**
     * 字典键值
     */
    public String dictValue;

    /**
     * 字典类型
     */
    public String dictType;

    /**
     * 样式属性（其他样式扩展）
     */
    public String cssClass;

    /**
     * 表格字典样式
     */
    public String listClass;

    /**
     * 是否默认（Y是 N否）
     */
    public String isDefault;

    /**
     * 状态（0正常 1停用）
     */
    public String status;

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


    @Override
    public void verify() {

    }
}
