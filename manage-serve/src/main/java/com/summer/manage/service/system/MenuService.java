package com.summer.manage.service.system;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.summer.common.core.BaseService;
import com.summer.common.exception.ThinkerException;
import com.summer.common.helper.BeanHelper;
import com.summer.common.helper.MathHelper;
import com.summer.common.helper.StringHelper;
import com.summer.common.view.parser.RequestContext;
import com.summer.common.view.parser.RequestSession;
import com.summer.manage.dao.system.SysMenuDAO;
import com.summer.manage.dao.system.SysRoleDAO;
import com.summer.manage.dao.system.SysUserDAO;
import com.summer.manage.dto.request.MenuRequest;
import com.summer.manage.dto.response.MenuResponse;
import com.summer.manage.dto.response.MetaDTO;
import com.summer.manage.dto.response.RouterResponse;
import com.summer.manage.dto.response.TreeSelectResponse;
import com.summer.manage.entity.system.SysMenu;
import com.summer.manage.entity.system.SysRole;
import com.summer.manage.entity.system.SysUser;
import com.summer.manage.kern.CodeMSG;
import com.summer.manage.kern.IConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Description：
 * @Author：sacher
 * @Create：2021/1/6 3:05 下午
 **/
@Service
public class MenuService extends BaseService {
    @Inject
    private SysMenuDAO sysMenuDAO;

    @Inject
    private SysUserDAO sysUserDAO;

    @Inject
    private SysRoleDAO sysRoleDAO;

    public Set<String> selectMenuPermsByUserId(Long id) {
        List<String> perms = sysMenuDAO.selectMenuPermsByUserId(id);
        Set<String> result = Sets.newHashSet();
        for (String s : perms) {
            if (!StringHelper.isBlank(s)) {
                result.addAll(Arrays.asList(s.trim().split(",")));
            }
        }
        return result;
    }

    public List<RouterResponse> getRouters() {
        RequestSession session = RequestContext.get().getSession();
        SysUser sysUser = sysUserDAO.getSysUserById(Long.parseLong(session.uid));
        List<SysMenu> menus;
        if (sysUser.isAdmin()) {
            menus = sysMenuDAO.getMenuTreeAll();
        } else {
            menus = sysMenuDAO.selectMenuTreeByUserId(sysUser.getId());
        }
        List<SysMenu> childPerms = getChildPerms(menus, 0);
        return buildMenus(childPerms);
    }

    /**
     * 获取路由名称
     *
     * @param menu 菜单信息
     * @return 路由名称
     */
    private String getRouteName(SysMenu menu) {
        String routerName = StringUtils.capitalize(menu.path);
        // 非外链并且是一级目录（类型为目录）
        if (isMeunFrame(menu)) {
            routerName = StringUtils.EMPTY;
        }
        return routerName;
    }

    /**
     * 是否为菜单内部跳转
     *
     * @param menu 菜单信息
     * @return 结果
     */
    private boolean isMeunFrame(SysMenu menu) {
        return menu.parentId.intValue() == 0
                && IConstant.UserMenu.TYPE_MENU.equals(menu.menuType)
                && menu.isFrame.equals(IConstant.UserMenu.NO_FRAME);
    }

    /**
     * 获取路由地址
     *
     * @param menu 菜单信息
     * @return 路由地址
     */
    private String getRouterPath(SysMenu menu) {
        String routerPath = menu.path;
        // 非外链并且是一级目录（类型为目录）
        if (0 == menu.parentId.intValue()
                && IConstant.UserMenu.TYPE_DIR.equals(menu.menuType)
                && IConstant.UserMenu.NO_FRAME.equals(menu.isFrame)) {
            routerPath = "/" + menu.path;
        } else if (isMeunFrame(menu)) {
            // 非外链并且是一级目录（类型为菜单）
            routerPath = "/";
        }
        return routerPath;
    }

    /**
     * 获取组件信息
     *
     * @param menu 菜单信息
     * @return 组件信息
     */
    private String getComponent(SysMenu menu) {
        String component = IConstant.UserMenu.LAYOUT;
        if (StringUtils.isNotEmpty(menu.component) && !isMeunFrame(menu)) {
            component = menu.component;
        }
        return component;
    }

    private List<RouterResponse> buildMenus(List<SysMenu> menus) {
        List<RouterResponse> routers = Lists.newLinkedList();
        for (SysMenu menu : menus) {
            RouterResponse router = new RouterResponse();
            router.hidden = "1".equals(menu.visible);
            router.name = getRouteName(menu);
            router.path = getRouterPath(menu);
            router.component = (getComponent(menu));
            router.meta = new MetaDTO(menu.menuName, menu.icon, StringUtils.equals("1", menu.isCache));
            List<SysMenu> cMenus = menu.children;
            if (!cMenus.isEmpty() && IConstant.UserMenu.TYPE_DIR.equals(menu.menuType)) {
                router.alwaysShow = true;
                router.redirect = "noRedirect";
                router.children = buildMenus(cMenus);
            } else if (isMeunFrame(menu)) {
                List<RouterResponse> childrenList = Lists.newArrayList();
                RouterResponse children = new RouterResponse();
                children.path = menu.path;
                children.component = menu.component;
                children.name = StringUtils.capitalize(menu.path);
                children.meta = new MetaDTO(menu.menuName, menu.icon, StringUtils.equals("1", menu.isCache));
                childrenList.add(children);
                router.children = childrenList;
            }
            routers.add(router);
        }
        return routers;
    }

    private List<SysMenu> getChildPerms(List<SysMenu> list, int parentId) {
        List<SysMenu> returnList = Lists.newArrayList();
        for (SysMenu t : list) {
            // 一、根据传入的某个父节点ID,遍历该父节点的所有子节点
            if (t.parentId == parentId) {
                recursionFn(list, t);
                returnList.add(t);
            }
        }
        return returnList;
    }

    /**
     * 递归列表
     *
     * @param list
     * @param t
     */
    private void recursionFn(List<SysMenu> list, SysMenu t) {
        // 得到子节点列表
        List<SysMenu> childList = getChildList(list, t);
        t.children = childList;
        for (SysMenu tChild : childList) {
            if (hasChild(list, tChild)) {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 得到子节点列表
     */
    private List<SysMenu> getChildList(List<SysMenu> list, SysMenu t) {
        List<SysMenu> tList = Lists.newArrayList();
        for (SysMenu n : list) {
            if (n.parentId.longValue() == t.getId().longValue()) {
                tList.add(n);
            }
        }
        return tList;
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<SysMenu> list, SysMenu t) {
        return getChildList(list, t).size() > 0;
    }

    public List<MenuResponse> listMenu(MenuRequest request) {
        List<SysMenu> menuList;
        SysUser sysUser = new SysUser(Long.parseLong(RequestContext.get().getSession().uid));
        if (sysUser.isAdmin()) {
            menuList = sysMenuDAO.selectMenuList(request);
        } else {
            request.getParams().put("userId", sysUser.getId());
            menuList = sysMenuDAO.selectMenuListByUserId(request);
        }
        return BeanHelper.castTo(menuList, MenuResponse.class);
    }

    public Integer add(MenuRequest menu) {
        if (IConstant.Common.NOT_UNIQUE.equals(checkMenuNameUnique(menu))) {
            throw new ThinkerException(CodeMSG.MenuRepeat);
        } else if (IConstant.Common.YES_FRAME.equals(menu.isFrame)
                && !StringUtils.startsWithAny(menu.path, IConstant.Common.HTTP, IConstant.Common.HTTPS)) {
            throw new ThinkerException(CodeMSG.YesFrame);
        }
        menu.createdBy = RequestContext.get().getSession().ext;
        SysMenu sysMenu = BeanHelper.castTo(menu, SysMenu.class);
        return sysMenuDAO.insertMenu(sysMenu);
    }

    private String checkMenuNameUnique(MenuRequest menu) {
        SysMenu info = sysMenuDAO.checkMenuNameUnique(menu.menuName, menu.parentId);
        if (Objects.nonNull(info) && info.getId() != MathHelper.nvl(menu.id)) {
            return IConstant.Common.NOT_UNIQUE;
        }
        return IConstant.Common.UNIQUE;
    }

    public Integer edit(MenuRequest menu) {
        if (IConstant.Common.NOT_UNIQUE.equals(checkMenuNameUnique(menu))) {
            throw new ThinkerException(CodeMSG.MenuRepeat);
        } else if (IConstant.Common.YES_FRAME.equals(menu.isFrame)
                && !StringUtils.startsWithAny(menu.path, IConstant.Common.HTTP, IConstant.Common.HTTPS)) {
            throw new ThinkerException(CodeMSG.YesFrame);
        } else if (menu.id.equals(menu.parentId)) {
            throw new ThinkerException(CodeMSG.MenuError);
        }
        menu.updatedBy = RequestContext.get().getSession().ext;
        SysMenu sysMenu = BeanHelper.castTo(menu, SysMenu.class);
        return sysMenuDAO.updateMenu(sysMenu);
    }

    public Integer remove(Long menuId) {
        if (sysMenuDAO.hasChildByMenuId(menuId) > 0) {
            throw new ThinkerException(CodeMSG.MenuSubRepeat);
        }
        if (sysMenuDAO.checkMenuExistRole(menuId) > 0) {
            throw new ThinkerException(CodeMSG.MenuIsUsing);
        }
        return sysMenuDAO.deleteMenuById(menuId);
    }

    public MenuResponse getMenu(Long menuId) {
        return BeanHelper.castTo(sysMenuDAO.selectMenuById(menuId), MenuResponse.class);
    }

    public List<TreeSelectResponse> treeSelect(MenuRequest request) {
        List<MenuResponse> menuResponses = listMenu(request);
        return buildMenuTreeSelect(menuResponses);
    }

    public List<TreeSelectResponse> buildMenuTreeSelect(List<MenuResponse> menus) {
        List<SysMenu> menuTrees = buildMenuTree(BeanHelper.castTo(menus, SysMenu.class));
        return menuTrees.stream().map(TreeSelectResponse::new).collect(Collectors.toList());
    }

    private List<SysMenu> buildMenuTree(List<SysMenu> menus) {
        List<SysMenu> returnList = Lists.newArrayList();
        List<Long> tempList = Lists.newArrayList();
        for (SysMenu sysMenu : menus) {
            tempList.add(sysMenu.getId());
        }
        for (Iterator<SysMenu> iterator = menus.iterator(); iterator.hasNext(); ) {
            SysMenu menu = iterator.next();
            // 如果是顶级节点, 遍历该父节点的所有子节点
            if (!tempList.contains(menu.parentId)) {
                recursionFn(menus, menu);
                returnList.add(menu);
            }
        }
        if (returnList.isEmpty()) {
            returnList = menus;
        }
        return returnList;
    }

    public Map<String, Object> roleMenuTreeSelect(Long id) {
        Map<String, Object> res = Maps.newHashMap();
        List<MenuResponse> menuResponses = listMenu(new MenuRequest());
        res.put("checkedKeys", selectMenuListByRoleId(id));
        res.put("menus", buildMenuTreeSelect(menuResponses));
        return res;
    }

    private List<Integer> selectMenuListByRoleId(Long id) {
        SysRole role = sysRoleDAO.selectRoleById(id);
        return sysMenuDAO.selectMenuListByRoleId(id, role.menuCheckStrictly);
    }

}
