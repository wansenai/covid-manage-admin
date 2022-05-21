package com.summer.common.esearch;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.summer.common.esearch.orm.Property;
import com.summer.common.support.NaturalizeLog;
import com.summer.common.helper.BytesHelper;
import com.summer.common.helper.DateHelper;
import com.summer.common.helper.EncryptHelper;
import com.summer.common.helper.StringHelper;
import com.summer.common.helper.ThreadFactoryHelper;
import com.summer.common.support.DateFormat;
import com.summer.common.support.OperationLog;
import javafx.util.Pair;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public final class EsearchOutputStream extends OutputStream {
    private static final String NL = StringHelper.camel2Underline(NaturalizeLog.class.getSimpleName()).substring(1);
    private static final String OL = StringHelper.camel2Underline(OperationLog.class.getSimpleName()).substring(1);
    private static final String OLF = OperationLog.class.getName();
    private static final int OLF_LEN = OLF.length();
    private static final Map<String, Class<?>> CLZ_MAP = ImmutableMap.of(NL, NaturalizeLog.class, OL, OperationLog.class);
    private static final Map<String, ?> SETTINGS = Collections.unmodifiableMap(new HashMap<String, Object>() {
        {
            put("index.number_of_shards", 5);
            put("index.refresh_interval", "5s");
            put("index.number_of_replicas", 1);
            put("index.max_result_window", 999999);
            put("index.translog.durability", "async");
            put("index.merge.scheduler.max_thread_count", 1);
        }
    });
    private static final TimeValue TIMEOUT = TimeValue.timeValueSeconds(5 * 1000L);
    private static final Set<String> INDEX_TYPE_SET = Sets.newHashSet();
    private static final int QUEUE_SIZE = 512;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(ThreadFactoryHelper.threadFactoryOf("LOG@ELASTIC"));
    private final BlockingQueue<Pair<String, String>> queue = new ArrayBlockingQueue<>(QUEUE_SIZE);
    private final TransportClient client;
    private final Future<?> task;
    private boolean loop = true;
    public EsearchOutputStream(String elasticURI) {
        this.client = EsearchFactory.transportClient(elasticURI);
        this.task = startingWrite2Elasticsearch();
    }

    @Override public void write(@SuppressWarnings("NullableProblems") byte[] ml) {
        if(null == ml || ml.length < 1) return;
        String log = BytesHelper.string(ml);
        try {
            if (log.startsWith(OLF)) {
                queue.put(new Pair<>(OL, log.substring(OLF_LEN)));
            } else {
                queue.put(new Pair<>(NL, log));
            }
        } catch (Exception e) {
            degrade2console(Lists.newArrayList(log), e);
        }
    }

    private Future<?> startingWrite2Elasticsearch() {
        return executor.submit(() -> {
            final List<String> sources = Lists.newArrayList();
            BulkRequestBuilder bulk = client.prepareBulk();
            do {
                try {
                    Pair<String, String> pair = queue.poll(5, TimeUnit.SECONDS);
                    if(null != pair && !StringHelper.isBlank(pair.getKey()) && !StringHelper.isBlank(pair.getValue())) {
                        String index = indexNameByType(pair.getKey());
                        if(!StringHelper.isBlank(index)) { String did = EncryptHelper.md5(pair.getValue());
                            bulk.add(client.prepareIndex(index, pair.getKey(), did).setSource(pair.getValue(), XContentType.JSON));
                            sources.add(pair.getValue()); if (sources.size() > QUEUE_SIZE) bulk = pushLog(sources, bulk);
                        } else {
                            System.out.println(pair.getValue());
                        }
                    } else {
                        if (sources.size() > 0) bulk = pushLog(sources, bulk);
                    }
                } catch (InterruptedException ie) {
                    System.out.println("LOG to elasticsearch error retry...");
                    ie.printStackTrace(); if (sources.size() > 0) {
                        try {
                            bulk = pushLog(sources, bulk);
                        } catch (Exception e) {
                            degrade2console(sources, e); bulk = pushLog(sources, null);
                        }
                    }
                }
            } while (loop);
        });
    }

    private BulkRequestBuilder pushLog(List<String> sources, BulkRequestBuilder bulk) {
        if(null != bulk) {
            BulkResponse response = bulk.get(TIMEOUT);
            if(response.hasFailures()) {
                for(BulkItemResponse item: response.getItems()) {
                    item.getFailure().getCause().printStackTrace();
                }
            }
        } sources.clear(); return client.prepareBulk();
    }

    @Override public void write(int b) {}
    @Override public void close() {
        loop = false; task.cancel(true); BytesHelper.close(client);
    }

    private void degrade2console(List<String> sources, Throwable cause) {
        System.out.println("LOG by elasticsearch error log degrade to console...");
        if(null != cause) cause.printStackTrace();
        for(String source: sources) System.out.println(source);
    }

    private String indexNameByType(String clzName) {
        String indexName = indexName(), indexType = indexName + "_" + clzName;
        if(!INDEX_TYPE_SET.contains(indexType)) synchronized (this) {
            try {
                IndicesAdminClient admin = client.admin().indices();
                if (!admin.prepareExists(indexName).get(TIMEOUT).isExists()) {
                    if (!admin.prepareCreate(indexName).setSettings(SETTINGS).get(TIMEOUT).isAcknowledged()) {
                        if (!admin.prepareExists(indexName).get(TIMEOUT).isExists()) {
                            System.out.println("LOG to elasticsearch failed to create index= " + indexName);
                            return null;
                        }
                    }
                }
                JSONObject properties = new JSONObject();
                Field[] fields = CLZ_MAP.get(clzName).getDeclaredFields();
                for (Field field : fields) {
                    Property property = field.getAnnotation(Property.class);
                    if (null != property) {
                        JSONObject propertyJson = new JSONObject();
                        propertyJson.put("include_in_all", property.all());
                        propertyJson.put("type", property.type().toString());
                        EomInitializer.analyzed(property, propertyJson, false);
                        properties.put(field.getName(), propertyJson);
                    }
                }
                Map mapping = ImmutableMap.of(clzName, ImmutableMap.of("dynamic", false, "properties", properties));
                if (!admin.preparePutMapping(indexName).setSource(mapping).setType(clzName).get(TIMEOUT).isAcknowledged()) {
                    System.out.println("LOG to elasticsearch failed to create index= " + indexName + " type= " + clzName);
                    return null;
                } else {
                    INDEX_TYPE_SET.add(indexType);
                }
            } catch (Exception e) {
                System.out.println("LOG to elasticsearch failed to create index= " + indexName);
                e.printStackTrace();
                return null;
            }
        }
        return indexName;
    }

    private String indexName() {
        Date now = DateHelper.now();
        String end = DateHelper.format(DateHelper.lastDayOfWeek(now), DateFormat.NumDate);
        String start = DateHelper.format(DateHelper.firstDayOfWeek(now), DateFormat.NumDate);
        return "log_week_" + start + "_" + end;
    }
}
