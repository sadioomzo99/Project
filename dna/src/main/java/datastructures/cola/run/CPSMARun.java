package datastructures.cola.run;

import core.BaseSequence;
import datastructures.KVEntry;
import datastructures.cola.core.Run;
import datastructures.cola.queryresult.RangeSearchResult;
import datastructures.cola.queryresult.StabbingSearchResult;
import datastructures.container.DNAContainer;
import datastructures.container.utils.DNAContainerUtils;
import datastructures.lightweight.index.PSMA;
import utils.Coder;

public class CPSMARun<V> extends Run.AbstractRun<Long, V> {
    private final PSMA psma;
    private final long arrayId;
    private final DNAContainer container;
    private final Coder<KVEntry<Long, V>, BaseSequence> coder;

    private CPSMARun(PSMA psma, int size, int level, long arrayId, DNAContainer container, Coder<KVEntry<Long, V>, BaseSequence> coder) {
        super(size, level);
        this.psma = psma;
        this.arrayId = arrayId;
        this.container = container;
        this.coder = coder;
    }

    @Override
    public KVEntry<Long, V> get(int index) {
        return coder.decode(DNAContainerUtils.getArrayPosUnchecked(container, arrayId, index));
    }

    @Override
    public Long getKey(int index) {
        return get(index).key();
    }

    @Override
    public V getValue(int index) {
        return get(index).value();
    }

    @Override
    public StabbingSearchResult<Long, V> search(Long key) {
        return PSMARun.search(key, this, psma);
    }

    @Override
    public RangeSearchResult.ResultPatch<Long, V> searchRange(Long keyLow, Long keyHigh) {
        return PSMARun.searchRange(keyLow, keyHigh, this, psma);
    }

    public PSMA getPsma() {
        return psma;
    }
    public long getArrayId() {
        return arrayId;
    }
    public DNAContainer getContainer() {
        return container;
    }

    public static class Builder<V> extends MemRun.Builder<Long, V> {
        private final Coder<KVEntry<Long, V>, BaseSequence> entryCoder;
        private final DNAContainer container;
        private final boolean parallelBuild;

        public Builder(int size, int level, boolean parallelBuild, DNAContainer container, Coder<KVEntry<Long, V>, BaseSequence> entryCoder) {
            super(size, level);
            this.entryCoder = entryCoder;
            this.container = container;
            this.parallelBuild = parallelBuild;
        }

        @Override
        public CPSMARun<V> build() {
            PSMA psma = new PSMA(entries.stream().map(KVEntry::key).toList());
            return new CPSMARun<>(psma, size, level, DNAContainerUtils.putArray(container, parallelBuild, entries.stream().map(entryCoder::encode).toArray(BaseSequence[]::new)), container, entryCoder);
        }
    }
}
