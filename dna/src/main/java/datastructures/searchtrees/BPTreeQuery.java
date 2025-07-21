package datastructures.searchtrees;

import java.util.stream.Stream;

public interface BPTreeQuery<K extends Comparable<K>, V> {
    V search(K key);
    Stream<V> search(K keyLow, K keyHigh);
}
