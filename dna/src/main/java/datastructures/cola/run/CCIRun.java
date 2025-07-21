package datastructures.cola.run;

import core.BaseSequence;
import datastructures.KVEntry;
import datastructures.cola.core.Run;
import datastructures.cola.queryresult.RangeSearchResult;
import datastructures.cola.queryresult.StabbingSearchResult;
import datastructures.container.DNAContainer;
import datastructures.container.utils.DNAContainerUtils;
import datastructures.lightweight.index.ColumnImprint;
import utils.Coder;

public class CCIRun<V> extends Run.AbstractRun<Double, V> {
    private final ColumnImprint ci;
    private final long arrayId;
    private final DNAContainer container;
    private final Coder<KVEntry<Double, V>, BaseSequence> coder;

    private CCIRun(ColumnImprint ci, int size, int level, long arrayId, DNAContainer container, Coder<KVEntry<Double, V>, BaseSequence> coder) {
        super(size, level);
        this.ci = ci;
        this.arrayId = arrayId;
        this.container = container;
        this.coder = coder;
    }

    @Override
    public KVEntry<Double, V> get(int index) {
        return coder.decode(DNAContainerUtils.getArrayPosUnchecked(container, arrayId, index));
    }

    @Override
    public Double getKey(int index) {
        return get(index).key();
    }

    @Override
    public V getValue(int index) {
        return get(index).value();
    }

    @Override
    public StabbingSearchResult<Double, V> search(Double key) {
        return CIRun.search(key, this, ci);
    }

    @Override
    public RangeSearchResult.ResultPatch<Double, V> searchRange(Double keyLow, Double keyHigh) {
       return CIRun.searchRange(keyLow, keyHigh, this, ci);
    }

    public ColumnImprint getCi() {
        return ci;
    }
    public long getArrayId() {
        return arrayId;
    }
    public DNAContainer getContainer() {
        return container;
    }

    public static class Builder<V> extends MemRun.Builder<Double, V> {
        private final Coder<KVEntry<Double, V>, BaseSequence> entryCoder;
        private final DNAContainer container;
        private final boolean parallelBuild;
        private final int numBits;

        public Builder(int size, int level, boolean parallelBuild, int numBits, DNAContainer container, Coder<KVEntry<Double, V>, BaseSequence> entryCoder) {
            super(size, level);
            this.entryCoder = entryCoder;
            this.container = container;
            this.parallelBuild = parallelBuild;
            this.numBits = numBits;
        }

        @Override
        public CCIRun<V> build() {
            ColumnImprint ci = new ColumnImprint(numBits, entries.stream().map(KVEntry::key).toList(), true);
            return new CCIRun<>(ci, size, level, DNAContainerUtils.putArray(container, parallelBuild, entries.stream().map(entryCoder::encode).toArray(BaseSequence[]::new)), container, entryCoder);
        }
    }
}
