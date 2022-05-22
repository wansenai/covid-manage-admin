package com.summer.manage.dao.system;

import com.summer.manage.dto.request.MenuRequest;
import com.summer.manage.entity.system.SysMenu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/28 12:14 上午
 **/
public interface SysMenuDAO {


    List<SysMenu> getMenuTreeAll();

    List<String> selectMenuPermsByUserId(@Param("id") Long id);

    List<SysMenu> selectMenuTreeByUserId(@Param("id") Long id);

    List<SysMenu> selectMenuList(MenuRequest request);

    List<SysMenu> selectMenuListByUserId(MenuRequest request);

    SysMenu checkMenuNameUnique(@Param("menuName") String menuName, @Param("parentId") Long parentId);

    int insertMenu(SysMenu sysMenu);

    Integer updateMenu(SysMenu sysMenu);

    int hasChildByMenuId(@Param("menuId") Long menuId);

    int checkMenuExistRole(@Param("menuId") Long menuId);

    int deleteMenuById(@Param("menuId") Long menuId);

    SysMenu selectMenuById(@Param("menuId") Long menuId);

    List<Integer> selectMenuListByRoleId(@Param("id") Long id, @Param("menuCheckStrictly") boolean menuCheckStrictly);
}
