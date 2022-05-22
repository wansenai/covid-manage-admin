package com.summer.common.esearch;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.summer.common.esearch.orm.EomModel;
import com.summer.common.helper.StringHelper;
import javafx.util.Pair;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;

public final class EsearchFactory {
    static final Map<String, EsearchOperations> ES_MAP = Maps.newConcurrentMap();
    private static final Logger LOG = LoggerFactory.getLogger(EsearchFactory.class);

    private EsearchFactory() {
    }

    public static EsearchOperations get(String esId) {
        return ES_MAP.get(esId);
    }

    public static List<String> indices(Class<? extends EomModel> clazz) {
        List<String> indices = Lists.newArrayList();
        String eomKey = EomInitializer.CIT_MAP.get(clazz).getKey();
        Pair<String, Integer> pars = EomInitializer.partitions(eomKey);
        for (int hash = 0; hash < pars.getValue(); hash++) {
            indices.add(pars.getKey() + hash);
        }
        return indices;
    }

    public static String typeName(Class<? extends EomModel> clazz) {
        return EomInitializer.CIT_MAP.get(clazz).getValue();
    }

    /**
     * ES://cluster@host:port,host:port,host:port
     */
    public static TransportClient transportClient(String uri) {
        if (StringHelper.isBlank(uri)) {
            throw new EsearchException("elasticsearch uri must not blank or null");
        }
        String clusterName = StringHelper.substringBetween(uri, "ES://", "@");
        if (StringHelper.isBlank(clusterName)) {
            throw new EsearchException("elasticsearch uri must format with ES://cluster@host:port,host:port,host:port ");
        }
        LOG.info("elasticsearch cluster name: {}", clusterName);
        String addresses = uri.substring(uri.indexOf("@") + 1);
        if (StringHelper.isBlank(addresses)) {
            throw new EsearchException("elasticsearch uri must format with ES://cluster@host:port,host:port,host:port ");
        }
        LOG.info("elasticsearch addresses: {}", addresses);
        Settings settings = Settings.builder().put("cluster.name", clusterName).build();
        TransportClient transportClient = new PreBuiltTransportClient(settings);
        String[] serverAddresses = addresses.split(",");
        for (String address : serverAddresses) {
            String[] serverAddress = address.split(":");
            int port = Integer.parseInt(serverAddress[1]);
            try {
                InetAddress host = InetAddress.getByName(serverAddress[0]);
                transportClient.addTransportAddress(new InetSocketTransportAddress(host, port));
            } catch (IOException e) {
                throw new EsearchException("elasticsearch uri host=" + serverAddress[0] + " as InetAddress error...", e);
            }
        }
        return transportClient;
    }
}
