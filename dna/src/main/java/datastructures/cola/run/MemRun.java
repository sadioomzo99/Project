package datastructures.cola.run;

import datastructures.KVEntry;
import datastructures.cola.core.Run;
import datastructures.cola.core.RunBuilder;
import java.util.ArrayList;
import java.util.List;

public class MemRun<K extends Comparable<K>, V> extends Run.AbstractRun<K, V> {

    private List<KVEntry<K, V>> entries;
    public MemRun(List<KVEntry<K, V>> entries, int level) {
        super(entries.size(), level);
        this.entries = entries;
    }

    public MemRun(KVEntry<K, V> entry, int level) {
        super(1, level);
        this.entries = new ArrayList<>(1);
        this.entries.add(entry);
    }

    @Override
    public K getKey(int index) {
        return get(index).key();
    }

    @Override
    public V getValue(int index) {
        return get(index).value();
    }

    @Override
    public KVEntry<K, V> get(int index) {
        return entries.get(index);
    }
    @Override
    public int size() {
        return entries.size();
    }


    @Override
    public Run<K, V> toMemRun() {
        return this;
    }

    public static class Builder<K extends Comparable<K>, V> extends RunBuilder.AbstractRunBuilder<K, V> {
        protected List<KVEntry<K, V>> entries;
        public Builder(int size, int level) {
            super(size, level);
            this.entries = new ArrayList<>(size);
        }

        public void link(MemRun<K, V> memRun) {
            this.entries = memRun.entries;
        }

        @Override
        public void add(KVEntry<K, V> entry) {
            this.entries.add(entry);
        }

        @Override
        public Run<K, V> build() {
            return new MemRun<>(entries, level);
        }
    }
}
