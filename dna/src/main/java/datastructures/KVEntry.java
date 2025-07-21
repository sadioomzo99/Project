package datastructures;

import utils.Pair;

public class KVEntry<K extends Comparable<K>, V> extends Pair<K, V> implements Comparable<KVEntry<K, V>> {

    public KVEntry(K k, V v) {
        super(k, v);
    }

    public K key() {
        return t1;
    }

    public V value() {
        return t2;
    }

    @Override
    public int compareTo(KVEntry<K, V> o) {
        return t1.compareTo(o.t1);
    }
}
