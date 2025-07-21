package datastructures.cola.run;

import datastructures.KVEntry;
import datastructures.cola.core.Run;
import datastructures.cola.queryresult.RangeSearchResult;
import datastructures.cola.queryresult.StabbingSearchResult;
import datastructures.lightweight.index.PSMA;
import utils.Pair;
import utils.Range;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class PSMARun<V> extends MemRun<Long, V> {
    private final PSMA psma;

    private PSMARun(PSMA psma, List<KVEntry<Long, V>> entries, int level) {
        super(entries, level);
        this.psma = psma;
    }

    @Override
    public StabbingSearchResult<Long, V> search(Long key) {
        return search(key, this, psma);
    }

    @Override
    public RangeSearchResult.ResultPatch<Long, V> searchRange(Long keyLow, Long keyHigh) {
        return searchRange(keyLow, keyHigh, this, psma);
    }

    public static <V> StabbingSearchResult<Long, V> search(Long key, Run<Long, V> run, PSMA psma) {
        Range<Integer> hotRange = psma.findDataRangeAsRange(key);
        if (hotRange.isEmpty())
            return StabbingSearchResult.noMatch(-1);

        return run.search(key, hotRange.getT1(), hotRange.getT2());
    }

    public static <V> RangeSearchResult.ResultPatch<Long, V> searchRange(Long keyLow, Long keyHigh, Run<Long, V> run, PSMA psma) {
        Stream<Pair<Integer, KVEntry<Long, V>>> st = psma
                .findDataRange(keyLow, keyHigh)
                .mapToObj(i -> new Pair<>(i, run.get(i)))
                .filter(p -> p.getT2().key().compareTo(keyLow) >= 0)
                .takeWhile(p -> p.getT2().key().compareTo(keyHigh) <= 0);

        return new RangeSearchResult.ResultPatch<>(run.level(), st);
    }

    public static class Builder<V> extends MemRun.Builder<Long, V> {
        private PSMA psma;
        private final boolean isSorted;

        public Builder(int size, int level, boolean isSorted) {
            super(size, level);
            this.isSorted = isSorted;
        }

        @Override
        public PSMARun<V> build() {
            if (!isSorted)
                Collections.sort(entries);
            this.psma = new PSMA(entries.stream().map(KVEntry::key).toList());
            return new PSMARun<>(psma, entries, level);
        }
    }
}
