package datastructures.cola.run.managers;

import datastructures.cola.core.Run;
import datastructures.cola.core.RunBuilder;
import datastructures.cola.runbuilderstrategy.RunBuilderStrategy;
import utils.Streamable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.IntStream;

public interface RunsManager<K extends Comparable<K>, V> extends Streamable<Run<K, V>> {
    void set(int level, Run<K, V> run);

    void removeRun(int level);
    RunBuilderStrategy<K, V> getRunBuilderStrategy();

    default void mergeAndSet(int level, Run<K, V> r1, Run<K, V> r2) {
        mergeAndSet(level, r1, r2, getRunBuilderStrategy().getRunBuilder(r1.size() + r2.size(), level));
    }
    void mergeAndSet(int level, Run<K, V> r1, Run<K, V> r2, RunBuilder<K, V> runBuilder);

    default void transformAndSet(int level, Run<K, V> run) {
        transformAndSet(level, run, getRunBuilderStrategy().getRunBuilder(run.size(), level));
    }
    void transformAndSet(int level, Run<K, V> run, RunBuilder<K, V> runBuilder);

    Run<K, V> get(int level);

    int size();

    boolean isEmpty();

    boolean isLevelFull(int level);


    default int[] getBusyLevelsTopDown() {
        return IntStream.range(0, size()).filter(this::isLevelFull).toArray();
    }

    default int[] getBusyLevelsBottomUp() {
        return IntStream.iterate(size() - 1, i -> i >= 0, i -> i - 1).filter(this::isLevelFull).toArray();
    }

    @Override
    default Iterator<Run<K, V>> iterator() {
        return Arrays.stream(getBusyLevelsTopDown()).boxed().map(this::get).iterator();
    }
}
