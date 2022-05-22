package com.summer.manage.dto.request;

import javax.validation.constraints.NotBlank;

/**
 * @Description：
 * @Author：sacher
 * @Create：2021/2/19 5:26 PM
 **/
public class FormulaRequest {
    /**
     * 当前页
     **/
    public Integer pager = 1;

    /**
     * 每页条数
     **/
    public Integer size = 20;

    /**
     * ID
     */
    public Long id;

    /**
     * 公式名称
     */
    @NotBlank(message = "请填写公式名称")
    public String formulaName;

    /**
     * 公式代号
     */
    @NotBlank(message = "请填写公式代号")
    public String formulaCode;

    /**
     * 公式表达式
     */
    @NotBlank(message = "请填写公式表达式")
    public String formulaExpression;

    /**
     * 公式计算
     */
    @NotBlank(message = "请填写公式公式计算方式")
    public String formulaCompute;

    /**
     * 公式取值范围
     */
    public String formulaRange;

}
