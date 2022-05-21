package javafx.util;

import javafx.beans.NamedArg;

import java.io.Serializable;
import java.util.Objects;

public class Pair<K,V> implements Serializable{
    private K key; private V value;

    public K getKey() { return key; }

    public V getValue() { return value; }

    public Pair(@NamedArg("key") K key, @NamedArg("value") V value) {
        this.key = key; this.value = value;
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }

    @Override
    public int hashCode() {
        return key.hashCode() * 13 + (value == null ? 0 : value.hashCode());
    }

     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o instanceof Pair) {
             Pair pair = (Pair) o;
             if (!Objects.equals(key, pair.key)) return false;
             if (!Objects.equals(value, pair.value)) return false;
             return true;
         }
         return false;
     }
 }

