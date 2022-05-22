package com.summer.manage.dto.response;


import com.google.common.collect.Lists;
import com.summer.common.core.BaseDTO;

import java.util.Date;
import java.util.List;

/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/27 6:03 下午
 **/
public class MenuResponse extends BaseDTO {
    /**
     * 主键ID
     **/
    public Long id;

    /**
     * 菜单名称
     */
    public String menuName;

    /**
     * 父菜单名称
     */
    public String parentName;

    /**
     * 父菜单ID
     */
    public Long parentId;

    /**
     * 显示顺序
     */
    public String orderNum;

    /**
     * 路由地址
     */
    public String path;

    /**
     * 组件路径
     */
    public String component;

    /**
     * 是否为外链（0是 1否）
     */
    public String isFrame;

    /**
     * 是否缓存（0缓存 1不缓存）
     */
    public String isCache;

    /**
     * 类型（M目录 C菜单 F按钮）
     */
    public String menuType;

    /**
     * 显示状态（0显示 1隐藏）
     */
    public String visible;

    /**
     * 菜单状态（0显示 1隐藏）
     */
    public String status;

    /**
     * 权限uri
     */
    public String uri;

    /**
     * 菜单图标
     */
    public String icon;

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

    public List<MenuResponse> children = Lists.newArrayList();
}
