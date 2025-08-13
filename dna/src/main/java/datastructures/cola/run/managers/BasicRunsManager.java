package datastructures.cola.run.managers;

import datastructures.cola.core.Run;
import datastructures.cola.core.RunBuilder;
import datastructures.cola.run.MemRun;
import datastructures.cola.runbuilderstrategy.RunBuilderStrategy;
import java.util.ArrayList;
import java.util.List;

public class BasicRunsManager<K extends Comparable<K>, V> implements RunsManager<K, V> {
    protected final List<Run<K, V>> runs;
    protected final RunBuilderStrategy<K, V> rbs;

    public BasicRunsManager(RunBuilderStrategy<K, V> rbs) {
        this.runs = new ArrayList<>();
        this.rbs = rbs;

    }

    @Override
    public void set(int level, Run<K, V> run) {
        while (level >= runs.size())
            runs.add(null);

        runs.set(level, run);
    }

    @Override
    public void removeRun(int level) {
        set(level, null);
    }

    @Override
    public void mergeAndSet(int level, Run<K, V> r1, Run<K, V> r2, RunBuilder<K, V> runBuilder) {
        set(level, r1.merge(r2, runBuilder));
    }

    @Override
    public void transformAndSet(int level, Run<K, V> run, RunBuilder<K, V> runBuilder) {
        if (run instanceof MemRun<K,V> memRun && runBuilder instanceof MemRun.Builder<K,V> rb)
            rb.link(memRun);
        else
            runBuilder.addAll(run);

        set(level, runBuilder.build());
    }

    @Override
    public Run<K, V> get(int level) {
        if (level < runs.size())
            return runs.get(level);

        return null;
    }

    @Override
    public RunBuilderStrategy<K, V> getRunBuilderStrategy() {
        return rbs;
    }

    @Override
    public int size() {
        return this.runs.size();
    }

    @Override
    public boolean isEmpty() {
        return this.runs.isEmpty();
    }

    @Override
    public boolean isLevelFull(int level) {
        return level < size() && get(level) != null;
    }
}
