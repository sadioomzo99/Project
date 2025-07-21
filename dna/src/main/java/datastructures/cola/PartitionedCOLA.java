package datastructures.cola;

import datastructures.cola.core.Run;
import datastructures.cola.run.FCRun;
import datastructures.cola.run.managers.RunsManager;
import java.util.stream.IntStream;

public class PartitionedCOLA<K extends Comparable<K>, V> extends BasicCOLA<K, V> {
    private final int partitionStartingLevel;
    private int lastMaxPartitionLevelToFC;

    public PartitionedCOLA(RunsManager<K, V> runsManager, int partitionStartingLevel) {
        this(runsManager, partitionStartingLevel, DEFAULT_FRACTIONAL_CASCADING);
    }

    public PartitionedCOLA(RunsManager<K, V> runsManager, int partitionStartingLevel, boolean fractionalCascading) {
        super(runsManager, fractionalCascading);
        this.partitionStartingLevel = partitionStartingLevel;
        this.lastMaxPartitionLevelToFC = -1;
    }

    @Override
    protected void merge(Run<K, V> merged) {
        int level = 0;
        Run<K, V> nextRun;
        do {
            if (addPartitionIfPossible(merged, level))
                return;

            nextRun = rm.get(level);
            if (mergeIntoFreeSpotIfPossible(merged, level, nextRun))
                return;

            if (mergeIntoTerminalNextLevelIfPossible(merged, level, nextRun))
                return;

            merged = merge(level, merged, nextRun);
            level++;
        } while(true);
    }

    private boolean addPartitionIfPossible(Run<K, V> merged, int level) {
        if (level >= partitionStartingLevel) {
            rm.transformAndSet(rm.size(), merged);
            return true;
        }
        return false;
    }

    @Override
    protected void fractionalCascade() {
        if (lastMaxPartitionLevelToFC == rm.size()) {
            super.fractionalCascade();
            return;
        }

        int fcIndex = IntStream.iterate(rm.size() - 2, l -> l >= 0, l -> l - 1).filter(this::isLevelFull).findFirst().orElse(-1);
        if (fcIndex < 0)
            return;

        Run<K, V> fc = rm.get(fcIndex);
        Run<K, V> target = rm.get(rm.size() - 1);
        this.rm.set(fcIndex, new FCRun<>(fc, target));
        this.lastMaxPartitionLevelToFC = rm.size();
    }
}
