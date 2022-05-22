package com.summer.manage.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.summer.manage.entity.system.SysDept;
import com.summer.manage.entity.system.SysMenu;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/28 3:56 下午
 **/
public class TreeSelectResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 节点ID
     */
    public Long id;

    /**
     * 节点名称
     */
    public String label;

    /**
     * 子节点
     */

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public List<TreeSelectResponse> children;

    public TreeSelectResponse(SysDept dept) {
        this.id = dept.getId();
        this.label = dept.deptName;
        this.children = dept.children.stream().map(TreeSelectResponse::new).collect(Collectors.toList());
    }

    public TreeSelectResponse() {

    }

    public TreeSelectResponse(SysMenu sysMenu) {
        this.id = sysMenu.getId();
        this.label = sysMenu.menuName;
        this.children = sysMenu.children.stream().map(TreeSelectResponse::new).collect(Collectors.toList());
    }
}
