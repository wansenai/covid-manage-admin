package com.summer.common.esearch;

import com.google.common.collect.Lists;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.sort.SortBuilder;

import java.util.List;

public class DataSearcher {
    public static final int MAX_SIZE_OFFSET = 10000;

    /**
     * 索引名列表
     **/
    String[] indexes;
    /**
     * ES数据类型列表
     **/
    String[] dataTypes;
    /**
     * Routing列表
     **/
    String[] routings;
    /**
     * 查询构建器
     **/
    QueryBuilder query;
    /**
     * 排序构建器
     **/
    List<SortBuilder> sorts = Lists.newArrayList();
    /**
     * 集合列表
     **/
    List<AggregationBuilder> aggregations = Lists.newArrayList();
    /**
     * 查询条数
     **/
    int size;
    /**
     * 跳过之前多少条数据
     **/
    int offset;

    private DataSearcher() {
    }

    public static DataSearcher make() {
        return new DataSearcher();
    }

    public DataSearcher ofIndexes(String... indexes) {
        this.indexes = indexes;
        return this;
    }

    public DataSearcher ofDataTypes(String... dataTypes) {
        this.dataTypes = dataTypes;
        return this;
    }

    public DataSearcher ofRoutings(String... routings) {
        this.routings = routings;
        return this;
    }

    public DataSearcher ofQuery(QueryBuilder query) {
        this.query = query;
        return this;
    }

    public DataSearcher appendSort(SortBuilder sort) {
        this.sorts.add(sort);
        return this;
    }

    public DataSearcher appendAggregation(AggregationBuilder agg) {
        this.aggregations.add(agg);
        return this;
    }

    public DataSearcher ofSize(int size) {
        if ((size + this.offset) > MAX_SIZE_OFFSET) {
            throw new ElasticsearchException("Result window is too large (size + offset must less then " + MAX_SIZE_OFFSET + ")");
        }
        this.size = size;
        return this;
    }

    public DataSearcher ofOffset(int offset) {
        if ((size + this.offset) > MAX_SIZE_OFFSET) {
            throw new ElasticsearchException("Result window is too large (size + offset must less then " + MAX_SIZE_OFFSET + ")");
        }
        this.offset = offset;
        return this;
    }
}
