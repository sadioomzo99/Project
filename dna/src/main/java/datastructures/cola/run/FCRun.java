package datastructures.cola.run;

import datastructures.cola.core.FractionalCascadingRun;
import datastructures.KVEntry;
import datastructures.cola.core.Run;
import utils.BufferedIterator;
import java.util.Iterator;
import java.util.TreeMap;

public class FCRun<K extends Comparable<K>, V> implements FractionalCascadingRun<K, V> {
    private final Run<K, V> run;
    private TreeMap<K, Integer> fcIndex;

    public FCRun(Run<K, V> run, Run<K, V> target) {
        this.fcIndex = new TreeMap<>();
        this.run = run;
        updateFC(target);
    }

    public FCRun(Run<K, V> run, TreeMap<K, Integer> fcIndex) {
        this.run = run;
        this.fcIndex = fcIndex;
    }

    @Override
    public KVEntry<K, V> get(int index) {
        return run.get(index);
    }

    @Override
    public K getKey(int index) {
        return run.getKey(index);
    }

    @Override
    public V getValue(int index) {
        return run.getValue(index);
    }

    @Override
    public int size() {
        return this.run.size();
    }

    @Override
    public int level() {
        return run.level();
    }

    @Override
    public void deallocate() {
        run.deallocate();
        fcIndex = null;
    }

    public Run<K, V> getInnerRun() {
        return run;
    }

    public int getFCLow(K key) {
        var e = fcIndex.floorEntry(key);
        return e != null ? e.getValue() : -1;
    }

    public int getFCHigh(K key) {
        var e = fcIndex.ceilingEntry(key);
        return e != null ? e.getValue() : -1;
    }

    @Override
    public void updateFC(Run<K, V> target) {
        int targetSize = target.size();
        int everyNthElement = 8;
        Iterator<KVEntry<K, V>> it = BufferedIterator.skipPeriodically(target.bufferedIterator(), everyNthElement - 1);

        int i = everyNthElement - 1;
        fcIndex.put(target.getKey(0), 0);
        while(it.hasNext()) {
            fcIndex.put(it.next().key(), i);
            i += everyNthElement;
        }
        int targetSize_1 = targetSize - 1;
        if (i != targetSize_1 + everyNthElement) {
            fcIndex.put(target.getKey(targetSize_1), targetSize_1);
        }
    }

    @Override
    public Run<K, V> toMemRun() {
        return new FCRun<>(run.toMemRun(), fcIndex);
    }

    @Override
    public String toString() {
        return "FCRun{" +
                run +
                ", fcIndex=" + fcIndex +
                '}';
    }
}
