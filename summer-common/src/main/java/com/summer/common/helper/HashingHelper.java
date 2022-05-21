package com.summer.common.helper;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.SortedMap;

/** 带虚拟节点的分布式一致性 Hash运算 **/
public final class HashingHelper {
    private static final Logger LOG = LoggerFactory.getLogger(HashingHelper.class);

    private HashingHelper() {
    }

    /** 顺时针取 Hash 环的值 **/
    public static <K, T> T targetNode(K v, final SortedMap<Integer, T> hashRingMap){
        if(null == v || CollectsHelper.isNullOrEmpty(hashRingMap)){
            throw new RuntimeException("hash val and hash ring map must not null/empty.....");
        }
        SortedMap<Integer, T> tailMap = hashRingMap.tailMap(hash(v.toString()));
        int hash = tailMap.isEmpty() ? hashRingMap.firstKey() : tailMap.firstKey();
        return hashRingMap.get(hash);
    }

    /** 生成 HASH 环 **/
    public static <T> SortedMap<Integer, T> makeHashRing(final LinkedHashSet<T> nodes, int virtualNum){
        SortedMap<Integer, T> hashCircle = Maps.newTreeMap();
        for(T node: nodes) {
            if (virtualNum > 0) {
                for (int i = 0; i < virtualNum; i++) {
                    String vNode = node.toString() + "#" + i;
                    hashCircle.put(hash(vNode), node);
                }
            } else {
                hashCircle.put(hash(node.toString()), node);
            }
        }
        return hashCircle;
    }

    public static int hash(String src) {
        return murmurHashX86(src, src.length());
    }

    /** MurmurHash算法生成 HASH 值 **/
    private static final int c1 = 0xcc9e2d51, c2 = 0x1b873593;
    private static int murmurHashX86(String src, int len) {
        int h1 = 0x13579BDF, pos = 0, k1 = 0, k2, shift = 0, bits, nBytes = 0;
        while (pos < len) {
            int code = src.charAt(pos++);
            if (code < 0x80) {
                k2 = code; bits = 8;
            } else if (code < 0x800) {
                k2 = (0xC0 | (code >> 6)) | ((0x80 | (code & 0x3F)) << 8); bits = 16;
            } else if (code < 0xD800 || code > 0xDFFF || pos >= len) {
                k2 = (0xE0 | (code >> 12)) | ((0x80 | ((code >> 6) & 0x3F)) << 8) | ((0x80 | (code & 0x3F)) << 16); bits = 24;
            } else {
                int utf32 = (int) src.charAt(pos++); utf32 = ((code - 0xD7C0) << 10) + (utf32 & 0x3FF);
                k2 = (0xff & (0xF0 | (utf32 >> 18))) | ((0x80 | ((utf32 >> 12) & 0x3F))) << 8 | ((0x80
                        | ((utf32 >> 6) & 0x3F))) << 16 | (0x80 | (utf32 & 0x3F)) << 24; bits = 32;
            }
            k1 |= k2 << shift;
            shift += bits;
            if (shift >= 32) {
                k1 *= c1; k1 = (k1 << 15) | (k1 >>> 17); k1 *= c2; h1 ^= k1; h1 = (h1 << 13) | (h1 >>> 19); h1 = h1 * 5 + 0xe6546b64; shift -= 32;
                if (shift != 0) {
                    k1 = k2 >>> (bits - shift); // bits used == bits - newshift
                } else {
                    k1 = 0;
                }
                nBytes += 4;
            }
        }
        if (shift > 0) {
            nBytes += shift >> 3; k1 *= c1; k1 = (k1 << 15) | (k1 >>> 17); k1 *= c2; h1 ^= k1;
        }
        h1 ^= nBytes; h1 ^= h1 >>> 16; h1 *= 0x85ebca6b; h1 ^= h1 >>> 13; h1 *= 0xc2b2ae35; h1 ^= h1 >>> 16;
        return h1;
    }
}
