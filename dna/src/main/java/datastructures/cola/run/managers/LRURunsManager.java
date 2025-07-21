package datastructures.cola.run.managers;

import datastructures.KVEntry;
import datastructures.cola.core.Run;
import datastructures.cola.core.RunBuilder;
import datastructures.cola.run.MemRun;
import datastructures.cola.runbuilderstrategy.RunBuilderStrategy;
import utils.LRUCache;

public class LRURunsManager<K extends Comparable<K>, V> extends BasicRunsManager<K, V> {
    protected final LRUCache<Integer, Run<K, V>> lru;

    public LRURunsManager(int cacheSize, RunBuilderStrategy<K, V> rbs) {
        super(rbs);
        if (cacheSize <= 0)
            throw new RuntimeException("LRU cache size cannot be <= 0");
        this.lru = new LRUCache<>(cacheSize);
    }

    @Override
    public void removeRun(int level) {
        super.removeRun(level);
        this.lru.remove(level);
    }

    @Override
    public Run<K, V> get(int level) {
        Run<K, V> run = null;
        if (level < runs.size()) {
            run = lru.get(level);
            if (run == null) {
                run = runs.get(level);
                lru.put(level, run);
            }
        }

        return run;
    }

    @Override
    public void set(int level, Run<K, V> run) {
        super.set(level, run);
        lru.update(level, run);
    }

    @Override
    public void mergeAndSet(int level, Run<K, V> r1, Run<K, V> r2, RunBuilder<K, V> runBuilder) {
        LRUAwareBuilder b = new LRUAwareBuilder(r1.size() + r2.size(), level, runBuilder);
        r1.merge(r2, b);
        super.set(level, b.build());
    }

    @Override
    public void transformAndSet(int level, Run<K, V> run, RunBuilder<K, V> runBuilder) {
        LRUAwareBuilder b = new LRUAwareBuilder(run.size(), level, runBuilder);
        b.addAll(run);
        super.set(level, b.build());
    }

    @Override
    public boolean isLevelFull(int level) {
        return level < size() && runs.get(level) != null;
    }

    private class LRUAwareBuilder extends MemRun.Builder<K, V> {
        private final RunBuilder<K, V> rb;
        private LRUAwareBuilder(int size, int level, RunBuilder<K, V> rb) {
            super(size, level);
            this.rb = rb;
        }

        @Override
        public void link(MemRun<K, V> memRun) {
            super.link(memRun);
            rb.addAll(memRun);
        }

        @Override
        public void add(KVEntry<K, V> entry) {
            super.add(entry);
            this.rb.add(entry);
        }

        @Override
        public void addAll(Run<K, V> run) {
            run.bufferedIterator().forEachRemaining(entries::add);
            this.rb.addAll(run);
        }

        @Override
        public Run<K, V> build() {
            lru.update(level, super.build());
            return rb.build();
        }
    }
}
