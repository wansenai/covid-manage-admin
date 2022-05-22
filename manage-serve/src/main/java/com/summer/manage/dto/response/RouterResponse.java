package com.summer.manage.dto.response;


import com.summer.common.core.BaseDTO;

import java.util.List;

/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/28 3:56 下午
 **/
public class RouterResponse extends BaseDTO {
    /**
     * 路由名字
     */
    public String name;

    /**
     * 路由地址
     */
    public String path;

    /**
     * 是否隐藏路由，当设置 true 的时候该路由不会再侧边栏出现
     */
    public boolean hidden;

    /**
     * 重定向地址，当设置 noRedirect 的时候该路由在面包屑导航中不可被点击
     */
    public String redirect;

    /**
     * 组件地址
     */
    public String component;

    /**
     * 当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式--如组件页面
     */
    public Boolean alwaysShow;

    /**
     * 其他元素
     */
    public MetaDTO meta;

    /**
     * 子路由
     */
    public List<RouterResponse> children;
}
