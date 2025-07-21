package datastructures.cola.run;

import datastructures.KVEntry;
import datastructures.cola.core.Run;
import datastructures.cola.queryresult.RangeSearchResult;
import datastructures.cola.queryresult.StabbingSearchResult;
import datastructures.lightweight.index.ColumnImprint;
import java.util.List;
import java.util.stream.Stream;

public class CIRun<V> extends MemRun<Double, V> {
    private final ColumnImprint ci;

    private CIRun(ColumnImprint ci, List<KVEntry<Double, V>> entries, int level) {
        super(entries, level);
        this.ci = ci;
    }

    @Override
    public StabbingSearchResult<Double, V> search(Double key) {
        return search(key, this, ci);
    }

    @Override
    public RangeSearchResult.ResultPatch<Double, V> searchRange(Double keyLow, Double keyHigh) {
        return searchRange(keyLow, keyHigh, this, ci);
    }

    public static <V> StabbingSearchResult<Double, V> search(Double key, Run<Double, V> run, ColumnImprint ci) {
        if (!ci.simplePointQuery(key))
            return StabbingSearchResult.noMatch(-1);

        return run.search(key);
    }

    public static <V> RangeSearchResult.ResultPatch<Double, V> searchRange(Double keyLow, Double keyHigh, Run<Double, V> run, ColumnImprint ci) {
        if (!ci.simpleRangeQuery(keyLow, keyHigh))
            return new RangeSearchResult.ResultPatch<>(-1, Stream.empty());

        return run.searchRange(keyLow, keyHigh);
    }

    public static class Builder<V> extends MemRun.Builder<Double, V> {
        private ColumnImprint ci;
        private final boolean isSorted;
        private final int numBits;

        public Builder(int size, int level, int numBits, boolean isSorted) {
            super(size, level);
            this.isSorted = isSorted;
            this.numBits = numBits;
        }

        @Override
        public CIRun<V> build() {
            this.ci = new ColumnImprint(numBits, entries.stream().map(KVEntry::key).toList(), isSorted);
            return new CIRun<>(ci, entries, level);
        }
    }
}
