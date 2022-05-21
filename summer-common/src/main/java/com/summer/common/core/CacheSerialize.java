package com.summer.common.core;

import com.alibaba.fastjson.annotation.JSONField;
import com.summer.common.helper.EncryptHelper;
import com.summer.common.helper.JsonHelper;
import com.summer.common.helper.StringHelper;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class CacheSerialize implements Serializable {
    protected transient String serializableId;

    @JSONField(serialize = false)
    public String getSerializableId() {
        return this.getClass().getSimpleName() + "_" + StringHelper.defaultString(serializableId);
    }

    public abstract void ofSerializableId();

    // 可缓存的列表
    public static class Flock<T> extends CacheSerialize implements Collection<T> {
        private final String serialId;
        private final Collection<T> collects;

        public Flock(Collection<T> collects) {
            this.collects = collects;
            this.serialId = serialId();
            ofSerializableId();
        }

        @Override
        public void ofSerializableId() {
            super.serializableId = serialId;
        }

        @Override
        public int size() {
            return collects.size();
        }

        @Override
        public boolean isEmpty() {
            return collects.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return collects.contains(o);
        }

        @Override
        public Iterator<T> iterator() {
            return collects.iterator();
        }

        @Override
        public Object[] toArray() {
            return collects.toArray();
        }

        @Override
        public <T1> T1[] toArray(T1[] a) {
            return collects.toArray(a);
        }

        @Override
        public boolean add(T t) {
            return collects.add(t);
        }

        @Override
        public boolean remove(Object o) {
            return collects.remove(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return collects.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            return collects.addAll(c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return collects.removeAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return collects.retainAll(c);
        }

        @Override
        public void clear() {
            collects.clear();
        }

        @Override
        public boolean removeIf(Predicate<? super T> filter) {
            return collects.removeIf(filter);
        }

        @Override
        public Spliterator<T> spliterator() {
            return collects.spliterator();
        }

        @Override
        public Stream<T> stream() {
            return collects.stream();
        }

        @Override
        public Stream<T> parallelStream() {
            return collects.parallelStream();
        }

        @Override
        public void forEach(Consumer<? super T> action) {
            collects.forEach(action);
        }

        @Override
        public String toString() {
            return collects.toString();
        }

        private String serialId() {
            return EncryptHelper.md5(JsonHelper.toJSONString(collects));
        }
    }
}
