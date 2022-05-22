package com.summer.common.esearch;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.summer.common.esearch.orm.QueryField;
import com.summer.common.helper.BeanHelper;
import com.summer.common.helper.CollectsHelper;
import com.summer.common.helper.SnowIdHelper;
import com.summer.common.helper.StringHelper;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetRequestBuilder;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.replication.ReplicationRequestBuilder;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class EsearchOperations {
    private static final Logger LOG = LoggerFactory.getLogger(EsearchOperations.class);
    private static final TimeValue TIMEOUT = TimeValue.timeValueSeconds(30 * 1000L);
    private final ConcurrentMap<Object, String> indexMap = Maps.newConcurrentMap();
    private final IndicesAdminClient admin;
    private final TransportClient client;
    private final boolean showS;

    EsearchOperations(TransportClient client, boolean showS) {
        this.showS = showS;
        this.client = client;
        this.admin = client.admin().indices();
    }

    /**
     * 创建INDEX
     **/
    @Deprecated
    public <T> String createIndex(final T dataKey, Function<T, String> indexNameStrategy, String indexBody) {
        if (null == dataKey || null == indexNameStrategy) {
            throw new EsearchException("create elasticsearch index dataKey and indexNameStrategy must not be null...");
        }
        if (!indexMap.containsKey(dataKey)) {
            indexMap.put(dataKey, createIndex(indexNameStrategy.apply(dataKey), indexBody));
        }
        return indexMap.get(dataKey);
    }

    /**
     * 创建INDEX
     **/
    public String createIndex(String indexName, String indexBody) {
        if (!indexExists(indexName)) synchronized (this) {
            if (!indexExists(indexName)) {
                CreateIndexRequest request = new CreateIndexRequest(indexName);
                request.source(indexBody, XContentType.JSON);
                CreateIndexResponse response = admin.create(request).actionGet(TIMEOUT);
                if (!response.isAcknowledged()) {
                    throw new EsearchException("failed to create index " + indexName);
                }
            }
        }
        return indexName;
    }

    /**
     * 创建 INDEX
     **/
    public String makeGetIndexWith(String indexName, JSONObject json) {
        if (!indexExists(indexName)) synchronized (this) {
            if (!indexExists(indexName)) {
                CreateIndexResponse response = admin.prepareCreate(indexName).setSource(json).get(TIMEOUT);
                if (!response.isAcknowledged()) {
                    throw new EsearchException("failed to create index " + indexName);
                }
            }
        }
        return indexName;
    }

    /**
     * 只创建 INDEX
     **/
    public String onlyMakeGetIndex(String indexName, JSONObject settings, JSONObject aliases) {
        if (!indexExists(indexName)) synchronized (this) {
            if (!indexExists(indexName)) {
                CreateIndexRequestBuilder builder = admin.prepareCreate(indexName);
                if (null != settings) {
                    builder.setSettings(settings);
                }
                if (null != aliases) {
                    builder.setAliases(aliases);
                }
                if (!builder.get(TIMEOUT).isAcknowledged()) {
                    throw new EsearchException("failed to create index " + indexName);
                }
            }
        }
        return indexName;
    }

    /**
     * 设置数据类型映射
     **/
    public boolean typeMappingPut(String indexName, String typeName, JSONObject mapping) {
        if (indexExists(indexName) && null != mapping && mapping.size() > 0) {
            PutMappingResponse response = admin.preparePutMapping(indexName)
                                               .setSource(mapping)
                                               .setType(typeName)
                                               .get(TIMEOUT);
            return response.isAcknowledged();
        }
        return false;
    }

    /**
     * 数据类型映射
     **/
    public Map<String, Object> typeMappingGet(String indexName, String typeName) {
        if (typeExists(indexName, typeName)) {
            GetMappingsResponse response = admin.prepareGetMappings(indexName).setTypes(typeName).get(TIMEOUT);
            return response.mappings().get(indexName).get(typeName).sourceAsMap();
        }
        return Maps.newHashMap();
    }

    /**
     * 删除所有INDEX
     **/
    public boolean deleteAllIndex() {
        boolean deleted = deleteIndex("_all");
        indexMap.clear();
        return deleted;
    }

    /**
     * 删除指定INDEX
     **/
    public boolean deleteIndex(String indexName) {
        if (indexExists(indexName)) {
            DeleteIndexResponse response = admin.prepareDelete(indexName).get(TIMEOUT);
            boolean deleted = response.isAcknowledged();
            if (deleted) {
                indexMap.remove(indexName);
            }
            return deleted;
        }
        indexMap.remove(indexName);
        return true;
    }

    /**
     * 创建或更新文档，如果 jsonId对应的文存在则更新，不存在则创建
     **/
    public String merged(final EsDocument document) {
        requestedBuilder(document).get(TIMEOUT);
        return document.docId;
    }

    /**
     * 批量 创建或更新文档
     **/
    public List<BulkItemResponse.Failure> multiMerged(List<EsDocument> documents, boolean immediate) {
        List<BulkItemResponse.Failure> failures = Lists.newArrayList();
        if (!CollectsHelper.isNullOrEmpty(documents)) {
            List<IndexRequestBuilder> builders = Lists.newArrayList();
            for (EsDocument document : documents) {
                builders.add(requestedBuilder(document));
            }
            return requestedBulk(builders, immediate, false);
        }
        return failures;
    }

    /**
     * 删除指定数据
     **/
    public int deleted(final EsDocument document) {
        DeleteRequestBuilder builder = deleteRequestBuilder(document);
        return RestStatus.OK == builder.get(TIMEOUT).status() ? 1 : 0;
    }

    /**
     * 批量删除数据
     **/
    public List<BulkItemResponse.Failure> multiDeleted(List<EsDocument> documents, boolean immediate) {
        List<BulkItemResponse.Failure> failures = Lists.newArrayList();
        if (!CollectsHelper.isNullOrEmpty(documents)) {
            List<DeleteRequestBuilder> builders = Lists.newArrayList();
            for (EsDocument document : documents) {
                builders.add(deleteRequestBuilder(document));
            }
            return requestedBulk(builders, immediate, true);
        }
        return failures;
    }

    /**
     * 查询操作
     **/
    public SearchResponse search(DataSearcher ds) {
        SearchRequestBuilder builder = client.prepareSearch();
        if (!CollectsHelper.isNullOrEmpty(ds.indexes)) {
            builder = builder.setIndices(ds.indexes);
        }
        if (!CollectsHelper.isNullOrEmpty(ds.dataTypes)) {
            builder = builder.setTypes(ds.dataTypes);
        }
        if (!CollectsHelper.isNullOrEmpty(ds.routings)) {
            builder = builder.setRouting(ds.routings);
        }
        if (null != ds.query) {
            builder = builder.setQuery(ds.query);
        }
        if (!ds.sorts.isEmpty()) {
            for (SortBuilder sort : ds.sorts) {
                builder = builder.addSort(sort);
            }
        }
        if (!ds.aggregations.isEmpty()) {
            for (AggregationBuilder agg : ds.aggregations) {
                builder = builder.addAggregation(agg);
            }
        }
        if (ds.size < 1) {
            LOG.warn("you will fetch 0 row data， because the size < 1");
        }
        builder = builder.setSize(ds.size < 1 ? 0 : ds.size);
        builder.setFrom(ds.offset);
        showS(builder);
        return builder.get(TIMEOUT);
    }

    /**
     * 批量获取
     **/
    public MultiGetResponse multiGet(List<DocumentIdx> didList) {
        MultiGetRequestBuilder builder = client.prepareMultiGet();
        if (!CollectsHelper.isNullOrEmpty(didList)) {
            for (DocumentIdx idx : didList) {
                if (!StringHelper.isBlank(idx.indexName) && !StringHelper.isBlank(idx.docId)) {
                    builder.add(idx.indexName, idx.dataType, idx.docId);
                }
            }
        }
        return builder.get(TIMEOUT);
    }

    /**
     * 获取指定文档
     **/
    public GetResponse oneGet(String indexName, String dataType, String documentId) {
        if (StringHelper.isBlank(indexName) || StringHelper.isBlank(dataType) || StringHelper.isBlank(documentId)) {
            throw new EsearchException("elasticsearch one get indices, dataType, documentId must not be empty/null...");
        }
        return client.prepareGet(indexName, dataType, documentId).get(TIMEOUT);
    }

    // and
    public BoolQueryBuilder eomQueryBuilderAND(Set<QueryField> conditions) {
        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        if (!CollectsHelper.isNullOrEmpty(conditions)) {
            for (QueryField field : conditions) {
                if (null != field && null != field.value && null != field.eql && !StringHelper.isBlank(field.propertyAt)) {
                    String name = StringHelper.camel2Underline(field.propertyAt);
                    boolean primitive = BeanHelper.isPrimitiveType(field.value.getClass());
                    switch (field.eql) {
                        case Included:
                            if (primitive) {
                                builder.must(QueryBuilders.termQuery(name, field.value));
                            } else if (field.value instanceof Collection && !((Collection) field.value).isEmpty()) {
                                builder.must(QueryBuilders.termsQuery(name, ((Collection) field.value).toArray()));
                            }
                            break;
                        case Excluded:
                            if (primitive) {
                                builder.mustNot(QueryBuilders.termQuery(name, field.value));
                            } else if (field.value instanceof Collection && !((Collection) field.value).isEmpty()) {
                                builder.mustNot(QueryBuilders.termsQuery(name, ((Collection) field.value).toArray()));
                            }
                            break;
                        case Greater:
                            if (primitive && StringHelper.isNumeric(field.value.toString())) {
                                builder.must(QueryBuilders.rangeQuery(name).gt(field.value));
                            }
                            break;
                        case GreaterE:
                            if (primitive && StringHelper.isNumeric(field.value.toString())) {
                                builder.must(QueryBuilders.rangeQuery(name).gte(field.value));
                            }
                            break;
                        case Little:
                            if (primitive && StringHelper.isNumeric(field.value.toString())) {
                                builder.must(QueryBuilders.rangeQuery(name).lt(field.value));
                            }
                            break;
                        case LittleE:
                            if (primitive && StringHelper.isNumeric(field.value.toString())) {
                                builder.must(QueryBuilders.rangeQuery(name).lte(field.value));
                            }
                            break;
                        case Matched:
                            if (primitive && !StringHelper.isBlank(field.value.toString())) {
                                String matcher = "*" + QueryParser.escape(field.value.toString()) + "*";
                                builder.must(QueryBuilders.wildcardQuery(name, matcher));
                            }
                            break;
                        case IsNull:
                            builder.mustNot(QueryBuilders.existsQuery(name));
                            break;
                    }
                }
            }
        }
        return builder;
    }

    // or
    public BoolQueryBuilder eomQueryOR(Set<QueryField> conditions) {
        BoolQueryBuilder builder = QueryBuilders.boolQuery();
        if (!CollectsHelper.isNullOrEmpty(conditions)) {
            for (QueryField field : conditions) {
                if (null != field && null != field.value && null != field.eql && !StringHelper.isBlank(field.propertyAt)) {
                    String name = StringHelper.camel2Underline(field.propertyAt);
                    boolean primitive = BeanHelper.isPrimitiveType(field.value.getClass());
                    switch (field.eql) {
                        case Included:
                            if (primitive) {
                                builder.should(QueryBuilders.termQuery(name, field.value));
                            } else if (field.value instanceof Collection && !((Collection) field.value).isEmpty()) {
                                builder.should(QueryBuilders.termsQuery(name, ((Collection) field.value).toArray()));
                            }
                            break;
                        case Excluded:
                            if (primitive) {
                                builder.should(QueryBuilders.boolQuery().mustNot(QueryBuilders.termQuery(name, field.value)));
                            } else if (field.value instanceof Collection && !((Collection) field.value).isEmpty()) {
                                builder.should(QueryBuilders.boolQuery().mustNot(QueryBuilders.termsQuery(name, ((Collection) field.value).toArray())));
                            }
                            break;
                        case Greater:
                            if (primitive && StringHelper.isNumeric(field.value.toString())) {
                                builder.should(QueryBuilders.rangeQuery(name).gt(field.value));
                            }
                            break;
                        case GreaterE:
                            if (primitive && StringHelper.isNumeric(field.value.toString())) {
                                builder.should(QueryBuilders.rangeQuery(name).gte(field.value));
                            }
                            break;
                        case Little:
                            if (primitive && StringHelper.isNumeric(field.value.toString())) {
                                builder.should(QueryBuilders.rangeQuery(name).lt(field.value));
                            }
                            break;
                        case LittleE:
                            if (primitive && StringHelper.isNumeric(field.value.toString())) {
                                builder.should(QueryBuilders.rangeQuery(name).lte(field.value));
                            }
                            break;
                        case Matched:
                            if (primitive && !StringHelper.isBlank(field.value.toString())) {
                                String matcher = "*" + QueryParser.escape(field.value.toString()) + "*";
                                builder.should(QueryBuilders.wildcardQuery(name, matcher));
                            }
                            break;
                        case IsNull:
                            builder.mustNot(QueryBuilders.existsQuery(name));
                            break;
                    }
                }
            }
        }
        return builder;
    }

    List<DiscoveryNode> listNodes() {
        return client.listedNodes();
    }

    private DeleteRequestBuilder deleteRequestBuilder(EsDocument document) {
        DeleteRequestBuilder builder = client.prepareDelete(document.indexName, document.dataType, document.docId);
        parentRoutingIntoBuilder(document, builder, true);
        return builder;
    }

    private IndexRequestBuilder requestedBuilder(EsDocument document) {
        document.ofDocumentId(StringHelper.defaultIfBlank(document.docId, SnowIdHelper.unique()));
        IndexRequestBuilder builder = client.prepareIndex(document.indexName, document.dataType, document.docId)
                                            .setSource(document.docJson, XContentType.JSON);
        parentRoutingIntoBuilder(document, builder, false);
        return builder;
    }

    private List<BulkItemResponse.Failure> requestedBulk(List<? extends ReplicationRequestBuilder> builders, boolean immediate, boolean deleted) {
        final BulkRequestBuilder bulk = client.prepareBulk();
        if (!CollectsHelper.isNullOrEmpty(builders)) {
            builders.forEach(builder -> {
                if (deleted) {
                    bulk.add((DeleteRequestBuilder) builder);
                } else {
                    bulk.add((IndexRequestBuilder) builder);
                }
            });
        }
        if (0 == bulk.numberOfActions()) {
            throw new EsearchException("elasticsearch dealing bulk all builds must not be empty/null...");
        }
        if (immediate) {
            bulk.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        }
        BulkResponse response = bulk.get(TIMEOUT);
        List<BulkItemResponse.Failure> failures = Lists.newArrayList();
        if (response.hasFailures()) {
            for (BulkItemResponse item : response.getItems()) {
                if (item.isFailed()) {
                    LOG.warn("failure: {}", item.getFailure().toString());
                    failures.add(item.getFailure());
                }
            }
        }
        return failures;
    }

    private void parentRoutingIntoBuilder(EsDocument document, ReplicationRequestBuilder builder, boolean deleted) {
        if (!StringHelper.isBlank(document.routing)) {
            if (deleted) {
                ((DeleteRequestBuilder) builder).setRouting(document.routing);
            } else {
                ((IndexRequestBuilder) builder).setRouting(document.routing);
            }
        }
        if (!StringHelper.isBlank(document.parent)) {
            if (deleted) {
                ((DeleteRequestBuilder) builder).setParent(document.parent);
            } else {
                ((IndexRequestBuilder) builder).setParent(document.parent);
            }
        }
    }

    public boolean indexExists(String indexName) {
        return admin.prepareExists(indexName).get(TIMEOUT).isExists();
    }

    private boolean typeExists(String indexName, String typeName) {
        return admin.prepareTypesExists(indexName).setTypes(typeName).get(TIMEOUT).isExists();
    }

    private void showS(ActionRequestBuilder builder) {
        if (showS) {
            System.out.println("elasticsearch query is: " + builder.toString());
        }
    }

    @PreDestroy
    @SuppressWarnings("unused")
    private void destroy() {
        if (null != client) {
            client.close();
        }
    }
}
