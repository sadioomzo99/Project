package datastructures.cola;

import datastructures.KVEntry;
import datastructures.cola.core.Run;
import datastructures.cola.run.FCRun;
import datastructures.cola.run.MemRun;
import datastructures.cola.run.managers.RunsManager;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BasicCOLA<K extends Comparable<K>, V> implements COLA<K, V> {

    public static final boolean DEFAULT_FRACTIONAL_CASCADING = false;

    protected long totalEntries;
    protected boolean fractionalCascading;
    protected RunsManager<K, V> rm;

    public BasicCOLA(RunsManager<K, V> runsManager) {
        this(runsManager, DEFAULT_FRACTIONAL_CASCADING);
    }

    public BasicCOLA(RunsManager<K, V> runsManager, boolean fractionalCascading) {
        this.totalEntries = 0L;
        this.fractionalCascading = fractionalCascading;
        this.rm = runsManager;
    }

    @Override
    public RunsManager<K, V> getRunsManager() {
        return rm;
    }

    @Override
    public void insert(K key, V value) {
        Run<K, V> insertRun = new MemRun<>(new KVEntry<>(key, value), 0);
        merge(insertRun);
        totalEntries++;
        if (fractionalCascading)
            fractionalCascade();
    }

    protected void merge(Run<K, V> merged) {
        int level = 0;
        Run<K, V> nextRun;
        do {
            nextRun = rm.get(level);
            if (mergeIntoFreeSpotIfPossible(merged, level, nextRun))
                return;

            if (mergeIntoTerminalNextLevelIfPossible(merged, level, nextRun))
                return;

            merged = merge(level, merged, nextRun);
            level++;
        } while(true);
    }

    protected boolean mergeIntoFreeSpotIfPossible(Run<K, V> merged, int level, Run<K, V> nextRun) {
        if (nextRun == null) {
            rm.transformAndSet(level, merged);
            return true;
        }
        return false;
    }

    protected boolean mergeIntoTerminalNextLevelIfPossible(Run<K, V> merged, int level, Run<K, V> nextRun) {
        int nextLevel = level + 1;
        if (nextLevel >= rm.size() || rm.get(nextLevel) == null) {
            rm.mergeAndSet(nextLevel, nextRun, merged);
            rm.removeRun(level);
            nextRun.deallocate();
            merged.deallocate();

            return true;
        }

        return false;
    }

    protected Run<K, V> merge(int level, Run<K, V> merged, Run<K, V> nextRun) {
        Run<K, V> beforeMerge = merged;
        merged = nextRun.merge(merged, new MemRun.Builder<>(nextRun.size() + merged.size(), level));
        beforeMerge.deallocate();
        nextRun.deallocate();
        rm.removeRun(level);
        return merged;
    }


    protected void fractionalCascade() {
        int[] levels = IntStream.range(0, rm.size()).filter(this::isLevelFull).limit(2).toArray();
        if (levels.length == 2)
            rm.set(levels[0], new FCRun<>(rm.get(levels[0]), rm.get(levels[1])));
    }

    @Override
    public long size() {
        return totalEntries;
    }


    @Override
    public String toString() {
        return "COLA {" +
                "\ntotalEntries=" + totalEntries +
                "\nruns:" + (size() == 0 ? "[]" : "\n" + IntStream.range(0, rm.size()).mapToObj(level -> {
                    Run<K, V> run = rm.get(level);
                    return "\tlevel: " + level + " -> " + (run == null ? "[]" : run.toString());
        }).collect(Collectors.joining("\n"))) +
                "\n}";
    }
}
