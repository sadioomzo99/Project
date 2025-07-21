package datastructures.cola.run;

import core.BaseSequence;
import datastructures.KVEntry;
import datastructures.cola.core.Run;
import datastructures.container.DNAContainer;
import datastructures.container.ccola.HCOLAHeaders;
import datastructures.container.utils.DNAContainerUtils;
import utils.Coder;
import utils.FuncUtils;
import java.util.stream.Stream;

public class CHRun<K extends Comparable<K>, V, H extends HCOLAHeaders.COLAHeader<?>> extends Run.AbstractRun<K, V> {
    private final Coder<KVEntry<K, V>, BaseSequence> entryCoder;
    private final Coder<H, BaseSequence> headerCoder;
    private final DNAContainer container;
    private final long runId;

    public CHRun(int size, int level, long runId, DNAContainer container, Coder<KVEntry<K, V>, BaseSequence> entryCoder, Coder<H, BaseSequence> headerCoder) {
        super(size, level);
        this.entryCoder = entryCoder;
        this.headerCoder = headerCoder;
        this.container = container;
        this.runId = runId;
    }

    @Override
    public KVEntry<K, V> get(int index) {
        return entryCoder.decode(DNAContainerUtils.getArrayPosUnchecked(container, runId, index + 1));
    }

    @Override
    public K getKey(int index) {
        return get(index).key();
    }

    @Override
    public V getValue(int index) {
        return get(index).value();
    }

    public H getHeader() {
        return headerCoder.decode(DNAContainerUtils.getArrayPosUnchecked(container, runId, 0));
    }

    @Override
    public String toString() {
        return getHeader() + " -- " + super.toString();
    }

    public static class Builder<K extends Comparable<K>, V, H extends HCOLAHeaders.COLAHeader<?>> extends MemRun.Builder<K, V> {
        private final Coder<KVEntry<K, V>, BaseSequence> entryCoder;
        private final Coder<H, BaseSequence> headerCoder;
        private final DNAContainer container;
        private final boolean parallelBuild;
        private final FuncUtils.TriFunction<Integer, Integer, Stream<KVEntry<K, V>>, H> headerGenerator;

        public Builder(int size, int level, boolean parallelBuild, DNAContainer container, Coder<KVEntry<K, V>, BaseSequence> entryCoder, Coder<H, BaseSequence> headerCoder, FuncUtils.TriFunction<Integer, Integer, Stream<KVEntry<K, V>>, H> headerGenerator) {
            super(size, level);
            this.entryCoder = entryCoder;
            this.headerCoder = headerCoder;
            this.container = container;
            this.parallelBuild = parallelBuild;
            this.headerGenerator = headerGenerator;
        }

        @Override
        public CHRun<K, V, H> build() {
            BaseSequence encodedHeader = headerCoder.encode(headerGenerator.apply(size, level, entries.stream()));
            BaseSequence[] array = new BaseSequence[entries.size() + 1];
            array[0] = encodedHeader;
            FuncUtils.enumerate(FuncUtils.stream(entries.stream(), parallelBuild).map(entryCoder::encode)).forEach(p -> array[p.getT1() + 1] = p.getT2());
            long runId = DNAContainerUtils.putArray(container, parallelBuild, array);
            return new CHRun<>(size, level, runId, container, entryCoder, headerCoder);
        }
    }
}
