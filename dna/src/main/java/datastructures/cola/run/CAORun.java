package datastructures.cola.run;

import core.BaseSequence;
import datastructures.KVEntry;
import datastructures.cola.core.Run;
import datastructures.container.DNAContainer;
import datastructures.container.ccola.CCOLAUtils;
import datastructures.container.utils.DNAContainerUtils;
import utils.Coder;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class CAORun<K extends Comparable<K>, V> extends Run.AbstractRun<K, V> {
    private final long arrayId;
    private final DNAContainer container;
    private final Coder<KVEntry<K, V>, BaseSequence> entryCoder;

    public CAORun(long arrayId, int level, int size, DNAContainer container, Coder<KVEntry<K, V>, BaseSequence> entryCoder) {
        super(size, level);
        this.arrayId = arrayId;
        this.container = container;
        this.entryCoder = entryCoder;
    }

    @Override
    public KVEntry<K, V> get(int index) {
        return entryCoder.decode(DNAContainerUtils.getArrayPosUnchecked(container, arrayId, index));
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
    public void deallocate() {
        throw new UnsupportedOperationException("cannot deallocate AOCOLA run");
    }


    @Override
    public String toString() {
        var array = Objects.requireNonNull(DNAContainerUtils.getArray(container, arrayId));
        return "CCOLARun{size=" + array.length + ", [" + Arrays.stream(array).map(entryCoder::decode).map(KVEntry::toString).collect(Collectors.joining(", ")) + "]}";
    }



    public static class Builder<K extends Comparable<K>, V> extends MemRun.Builder<K, V> {

        private final DNAContainer container;
        private final long colaId;
        private final Coder<KVEntry<K, V>, BaseSequence> entryCoder;
        private final boolean parallelBuild;

        public Builder(int size, int level, DNAContainer container, long colaId, Coder<KVEntry<K, V>, BaseSequence> entryCoder, boolean parallelBuild) {
            super(size, level);
            this.container = container;
            this.colaId = colaId;
            this.entryCoder = entryCoder;
            this.parallelBuild = parallelBuild;
        }

        @Override
        public CAORun<K, V> build() {
            var r = super.build();
            var arrayId = CCOLAUtils.appendAOCOLARun(container, colaId, r, entryCoder, parallelBuild);
            return new CAORun<>(arrayId, level, r.size(), container, entryCoder);
        }
    }
}
