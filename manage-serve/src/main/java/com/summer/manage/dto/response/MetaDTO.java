package com.summer.manage.dto.response;


import com.summer.common.core.BaseDTO;

/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/28 4:03 下午
 **/
public class MetaDTO extends BaseDTO {
    /**
     * 设置该路由在侧边栏和面包屑中展示的名字
     */
    public String title;

    /**
     * 设置该路由的图标，对应路径src/assets/icons/svg
     */
    public String icon;

    /**
     * 设置为true，则不会被 <keep-alive>缓存
     */
    public boolean noCache;

    public MetaDTO(String title, String icon, boolean noCache) {
        this.title = title;
        this.icon = icon;
        this.noCache = noCache;
    }

}
