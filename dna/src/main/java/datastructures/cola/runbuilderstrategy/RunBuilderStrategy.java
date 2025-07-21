package datastructures.cola.runbuilderstrategy;

import core.BaseSequence;
import datastructures.KVEntry;
import datastructures.cola.core.RunBuilder;
import datastructures.cola.run.*;
import datastructures.container.DNAContainer;
import datastructures.container.ccola.HCOLAHeaders;
import datastructures.container.utils.DNAContainerUtils;
import utils.Coder;
import utils.FuncUtils;

import java.util.stream.Stream;

public interface RunBuilderStrategy<K extends Comparable<K>, V> {
    RunBuilder<K, V> getRunBuilder(int size, int level);
    default RunBuilder<K, V> getRunBuilder(int level) {
        return getRunBuilder(1 << level, level);
    }

    static<K extends Comparable<K>, V> RunBuilderStrategy<K, V> memory() {
        return MemRun.Builder::new;
    }

    static<K extends Comparable<K>, V> RunBuilderStrategy<K, V> disk(String filePath, DiskRun.EntrySerializer<K, V> serializer) {
        return (size, level) -> new DiskRun.Builder<>(filePath + "." + level, serializer, level);
    }

    static<K extends Comparable<K>, V> RunBuilderStrategy<K, V> partitionedDNA_CAO(boolean parallel, int DNAStartLevel, DNAContainer container, Coder<KVEntry<K, V>, BaseSequence> entryCoder) {
        long colaId = DNAContainerUtils.putList(container);

        return (size, level) -> {
            if (level < DNAStartLevel)
                return new MemRun.Builder<>(size, level);

            return new CAORun.Builder<>(size, level, container, colaId, entryCoder, parallel);
        };
    }

    static<V> RunBuilderStrategy<Long, V> partitionedDNA_PSMA(boolean parallel, int DNAStartLevel, DNAContainer container, Coder<KVEntry<Long, V>, BaseSequence> entryCoder) {
        return (size, level) -> {
            if (level < DNAStartLevel)
                return new MemRun.Builder<>(size, level);

            return new CPSMARun.Builder<>(size, level, parallel, container, entryCoder);
        };
    }

    static<V> RunBuilderStrategy<Double, V> partitionedDNA_CI(boolean parallel, int DNAStartLevel, int numBits, DNAContainer container, Coder<KVEntry<Double, V>, BaseSequence> entryCoder) {
        return (size, level) -> {
            if (level < DNAStartLevel)
                return new MemRun.Builder<>(size, level);

            return new CCIRun.Builder<>(size, level, parallel, numBits, container, entryCoder);
        };
    }

    static<K extends Comparable<K>, V, H extends HCOLAHeaders.COLAHeader<?>> RunBuilderStrategy<K, V> partitionedDNA_Header(boolean parallel, int DNAStartLevel, DNAContainer container, Coder<KVEntry<K, V>, BaseSequence> entryCoder, Coder<H, BaseSequence> headerCoder, FuncUtils.TriFunction<Integer, Integer, Stream<KVEntry<K, V>>, H> headerGenerator) {
        return (size, level) -> {
            if (level < DNAStartLevel)
                return new MemRun.Builder<>(size, level);

            return new CHRun.Builder<>(size, level, parallel, container, entryCoder, headerCoder, headerGenerator);
        };
    }
}
