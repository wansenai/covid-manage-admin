package com.summer.common.ibatis;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.summer.common.helper.HashingHelper;

import java.util.LinkedHashSet;
import java.util.SortedMap;

public final class ShardHelper {
    /**
     * 分库数量, 分表数量, 虚拟节点数量
     **/
    private static final int DATASOURCE_NUM = 8, PARTITION_NUM = 8, VIRTUAL_NUM = 128;
    /**
     * 一致HASH环
     **/
    private static final SortedMap<Integer, ShardInfo> DS_MAP = Maps.newTreeMap();

    // 初始化HASH环
    static {
        LinkedHashSet<ShardInfo> nodes = Sets.newLinkedHashSet();
        for (int i = 0; i < DATASOURCE_NUM; i++) {
            for (int h = 0; h < PARTITION_NUM; h++) {
                nodes.add(new ShardInfo(i, h));
            }
        }
        DS_MAP.putAll(HashingHelper.makeHashRing(nodes, VIRTUAL_NUM));
    }

    private ShardHelper() {
    }

    /**
     * 根据TID获取分库分表信息
     **/
    public static ShardInfo shardInfo(String keyId) {
        return HashingHelper.targetNode(shardHashKey(keyId), DS_MAP);
    }

    //TID的HASH值 对 (8 * 8 * 128) 取余（hashX % 8192），如为负数则+8192补正
    private static int shardHashKey(String tid) {
        int part = HashingHelper.hash(String.valueOf(HashingHelper.hash(tid))) % 8192;
        return part < 0 ? part + 8192 : part;
    }
}
