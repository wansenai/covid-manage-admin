package com.summer.common.support;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * 分页条信息
 **/
public final class Pagination<T> {
    /**
     * 当前面
     **/
    @JSONField(ordinal = 0)
    private int pager;
    /**
     * 总页数
     **/
    @JSONField(ordinal = 1)
    private int pages;
    /**
     * 每页条数
     **/
    @JSONField(ordinal = 2)
    private int size;
    /**
     * 总条数
     **/
    @JSONField(ordinal = 3)
    private long total;
    /**
     * 过滤前多少条
     **/
    private transient int offset;
    /**
     * 数据列表
     **/
    @JSONField(ordinal = 4)
    private List<T> list = Lists.newArrayList();

    @SuppressWarnings("unused")
    public Pagination() {
    }

    private Pagination(int pager, int size) {
        if (pager < 1 || size < 1) {
            throw new RuntimeException("invalid pager: " + pager + " or size: " + size);
        }
        this.pager = pager;
        this.size = size;
    }

    public static Pagination create(int pager, int size) {
        return new Pagination(pager, size);
    }

    public int getPager() {
        return 0 == pager ? 1 : pager;
    }

    public int getSize() {
        return size;
    }

    public int getPages() {
        return Double.valueOf(Math.ceil((double) total / (double) size)).intValue();
    }

    public int getOffset() {
        return size * (getPager() - 1);
    }

    public List<T> getList() {
        return list;
    }

    @Deprecated
    public void setList(List<T> list) {
        this.list = list;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

}
