package utils;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class LRUCache<K, V> {
    private final Map<K, V> map;
    private final Deque<K> deque;
    private final int capacity;

    public LRUCache(int capacity) {
        this.map = new HashMap<>();
        this.deque = new LinkedList<>();
        this.capacity = capacity;
    }

    public V get(K key) {
        return this.map.get(key);
    }

    public void put(K key, V value) {
        if (deque.size() >= capacity) {
            K oldest = deque.removeFirst();
            map.remove(oldest);
        }
        map.put(key, value);
        deque.addLast(key);
    }

    public void update(K key, V value) {
        deque.remove(key);
        put(key, value);
    }

    public void remove(K key) {
        if (map.containsKey(key)) {
            map.remove(key);
            deque.remove(key);
        }
    }
}
