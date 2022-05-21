package com.summer.common.helper;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import javafx.util.Pair;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/** 集合类工具 **/
public final class CollectsHelper {
    private CollectsHelper() {
    }

    public static <T> boolean isNullOrEmpty(final Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }

    public static <T> boolean isNullOrEmpty(final T[] array) {
        return array == null || array.length == 0;
    }

    public static <K, V> boolean isNullOrEmpty(final Map<K, V> map) {
        return map == null || map.isEmpty();
    }

    public static <T> boolean contains(final T[] array, final T val) {
        for (T t : array) {
            if (val.equals(t)) {
                return true;
            }
        }
        return false;
    }

    public static <T> Optional<T> head(final List<T> list) {
        return isNullOrEmpty(list) ? Optional.empty() : Optional.ofNullable(list.get(0));
    }

    public static <T> Optional<T> end(final List<T> list) {
        return isNullOrEmpty(list) ? Optional.empty() : Optional.ofNullable(list.get(list.size() - 1));
    }

    public static <T, R> List<R> map(final Collection<T> list, Function<? super T, R> mapF) {
        return isNullOrEmpty(list) ? Lists.newArrayList() : list.stream().map(mapF).collect(Collectors.toList());
    }

    public static <T> List<T> filter(final List<T> list, Function<T, Boolean> compare) {
        if(null == compare) {
            return list;
        }
        if(!isNullOrEmpty(list)) {
            return list.stream().filter(t -> compare.apply(t)).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    public static <V> Map<Integer, V> asMap(final List<V> list) {
        Map<Integer, V> result = Maps.newHashMap();
        if(!isNullOrEmpty(list)) {
            int size = list.size();
            for (int idx = 0; idx < size; idx++) {
                result.put(idx, list.get(idx));
            }
        }
        return result;
    }

    public static <K, V> LinkedHashMap<K, V>  ofMap(final List<V> list, final Function<V, K> keyF) {
        LinkedHashMap<K, V> result = Maps.newLinkedHashMap();
        if(!isNullOrEmpty(list)) {
            for (V value : list) {
                result.put(keyF.apply(value), value);
            }
        }
        return result;
    }

    public static <K, V, R> ListMultimap<K, R> group(final List<V> list, final Function<V, Pair<K, R>> pairF) {
        ListMultimap<K, R> result = ArrayListMultimap.create();
        if(!isNullOrEmpty(list)) {
            for (V v : list) {
                Pair<K, R> pair = pairF.apply(v);
                result.put(pair.getKey(), pair.getValue());
            }
        }
        return result;
    }

    public static <T, K, U, A, R> Map<K, R> groupToCollector(final Collection<T> list,
                                                             final Function<? super T, K> keyFunc,
                                                             final Function<? super T, ? extends U> groupByEle,
                                                             final Collector<? super U, A, R> valueCol) {
        if(CollectsHelper.isNullOrEmpty(list)){
            return Maps.newHashMap();
        }
        return list.stream().collect(Collectors
                        .groupingBy(keyFunc,Collectors.mapping(groupByEle, valueCol)));

    }

    public static <T> void sort(List<T> list, Map<String, String> sortM) {
        if (isNullOrEmpty(list)) {
            return;
        }
        list.sort((o1, o2) -> {
            for(Map.Entry<String, String> sort: sortM.entrySet()){
                int sortV = "ASC".equalsIgnoreCase(sort.getValue()) ? 1 : ("DESC".equalsIgnoreCase(sort.getValue()) ? -1 : 0);
                // 不重新排序
                if ((0 == sortV) || (null == o1) || (null == o2) || BeanHelper.isPrimitiveType(o1.getClass())) {
                    continue;
                }
                Object o1Val = BeanHelper.getProperty(o1, sort.getKey());
                Object o2Val = BeanHelper.getProperty(o2, sort.getKey());
                if (null == o1Val || null == o2Val) {
                    continue;
                }
                // 时间排序
                if (o1Val instanceof Date) {
                    Date v1 = (Date) o1Val;
                    Date v2 = (Date) o2Val;
                    if (v1.compareTo(v2) == 0) {
                        continue;
                    }
                    return sortV > 0 ? v1.compareTo(v2) : v2.compareTo(v1);
                }
                // 数字排序
                else if (o1Val instanceof Number) {
                    BigDecimal v1 = new BigDecimal(o1Val.toString());
                    BigDecimal v2 = new BigDecimal(o2Val.toString());
                    if (0 == v1.compareTo(v2)) {
                        continue;
                    }
                    return sortV > 0 ? v1.compareTo(v2) : v2.compareTo(v1);
                }
                // 字符串排序
				else if (o1Val instanceof String){
					String v1 = o1Val.toString();
					String v2 = o2Val.toString();
					if (0 == v1.compareTo(v2)) {
						continue;
					}
					return sortV > 0 ? v1.compareTo(v2) : v2.compareTo(v1);
				}
            }
            return 0;
        });
    }
}
