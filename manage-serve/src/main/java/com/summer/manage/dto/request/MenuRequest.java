package com.summer.manage.dto.request;


import com.summer.common.core.BaseDTO;
import com.summer.common.core.IRequest;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * @Description：
 * @Author：sacher
 * @Create：2021/1/7 3:52 下午
 **/
public class MenuRequest extends BaseDTO implements IRequest {
    /**
     * 主键ID
     **/
    public Long id;


    /**
     * 菜单名称
     */
    @NotBlank(message = "菜单名称不能为空")
    @Size(min = 0, max = 50, message = "菜单名称长度不能超过50个字符")
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
    @NotBlank(message = "显示顺序不能为空")
    public String orderNum;

    /**
     * 路由地址
     */
    @Size(min = 0, max = 200, message = "路由地址不能超过200个字符")
    public String path;

    /**
     * 组件路径
     */
    @Size(min = 0, max = 255, message = "组件路径不能超过255个字符")
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
    @NotBlank(message = "菜单类型不能为空")
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
    @Size(min = 0, max = 500, message = "权限地址不能超过500个字符")
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

    @Override
    public void verify() {

    }
}
