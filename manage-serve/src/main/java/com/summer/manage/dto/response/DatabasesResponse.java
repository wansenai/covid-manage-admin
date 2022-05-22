package com.summer.manage.dto.response;


import com.summer.common.core.BaseDTO;

import java.util.List;

/**
 * @Description：
 * @Author：sacher
 * @Create：2020/10/27 6:03 下午
 **/
public class DatabasesResponse extends BaseDTO {
    public Integer id;
    public String label;
    public List<DatabasesResponse> children;
    public Boolean disabled;

    public DatabasesResponse(Integer id, String label) {
        this.id = id;
        this.label = label;
    }
}
