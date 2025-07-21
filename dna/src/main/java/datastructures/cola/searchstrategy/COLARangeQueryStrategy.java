package datastructures.cola.searchstrategy;

import datastructures.cola.COLA;
import datastructures.cola.core.FractionalCascadingRun;
import datastructures.cola.core.Run;
import datastructures.cola.queryresult.RangeSearchResult;
import datastructures.cola.queryresult.StabbingSearchResult;
import utils.Pair;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface COLARangeQueryStrategy<K extends Comparable<K>, V> {
    RangeSearchResult<K, V> search(K keyLow, K keyHigh, COLA<K, V> cola);


    static <K extends Comparable<K>, V> RangeSearchResult<K, V> topDown(K keyLow, K keyHigh, COLA<K, V> cola) {
        return searchLevels(keyLow, keyHigh, cola, cola.getBusyLevelsTopDown());
    }

    static <K extends Comparable<K>, V> RangeSearchResult<K, V> bottomUp(K keyLow, K keyHigh, COLA<K, V> cola) {
        return searchLevels(keyLow, keyHigh, cola, cola.getBusyLevelsBottomUp());
    }

    private static <K extends Comparable<K>, V> RangeSearchResult<K, V> searchLevels(K keyLow, K keyHigh, COLA<K, V> cola, int[] levels) {
        if (keyHigh.compareTo(keyLow) < 0)
            throw new RuntimeException("keyHigh < keyLow");

        return new RangeSearchResult<>(Arrays.stream(levels).mapToObj(cola::getRun).map(run -> run.searchRange(keyLow, keyHigh)));
    }

    static<K extends Comparable<K>, V> RangeSearchResult<K, V> fractionalCascade(K keyLow, K keyHigh, COLA<K, V> cola) {
        int compared = keyHigh.compareTo(keyLow);
        if (compared < 0)
            throw new RuntimeException("keyHigh < keyLow");

        if (compared == 0) {
            StabbingSearchResult<K, V> stabbingSearchResult = COLAStabbingQueryStrategy.fractionalCascade(keyLow, cola);
            if (stabbingSearchResult.isMatch())
                return stabbingSearchResult.isMatch() ?
                        new RangeSearchResult<>(Stream.of(new RangeSearchResult.ResultPatch<>(stabbingSearchResult.getLevel(), Stream.of(new Pair<>(stabbingSearchResult.getIndex(), stabbingSearchResult.getResult())))))
                        : RangeSearchResult.empty();

        }
        int[] levels = cola.getBusyLevelsTopDown();
        if (levels.length == 0)
            return RangeSearchResult.empty();

        Stream.Builder<RangeSearchResult.ResultPatch<K, V>> b = Stream.builder();
        Run<K, V> run = cola.getRun(levels[0]);
        int size = run.size();
        StabbingSearchResult<K, V> lowResult = run.search(keyLow);
        StabbingSearchResult<K, V> highResult = run.search(keyHigh);

        int l = lowResult.getIndexHigh();
        int low = lowResult.getIndex();
        int r = highResult.getIndexLow();
        if (low < size) {
            int high = highResult.getIndex();
            if (highResult.isNoMatch())
                high--;

            if (!(highResult.isNoMatch() && high == 0) || lowResult.isMatch()) {
                Run<K, V> finalRun = run;
                b.add(new RangeSearchResult.ResultPatch<>(run.level(), IntStream.rangeClosed(low, high).mapToObj(i -> new Pair<>(i, finalRun.get(i)))));
            }
        }

        int lastLevelIndex = levels.length - 1;
        for (int levelIndex = 1; levelIndex <= lastLevelIndex; levelIndex++) {
            run = cola.getRun(levels[levelIndex]);
            size = run.size();
            if (l != -1 && l != size && r != -1) {
                if (r == size)
                    r--;

                Run<K, V> finalRun = run;
                l = IntStream.iterate(l - 1, x -> x >= 0, x -> x - 1).filter(i -> finalRun.get(i).key().compareTo(keyLow) < 0).map(i -> i + 1).findFirst().orElse(l);
                r = IntStream.range(r + 1, size).filter(i -> finalRun.get(i).key().compareTo(keyHigh) > 0).map(i -> i - 1).findFirst().orElse(r);

                Run<K, V> fr = run;
                b.add(new RangeSearchResult.ResultPatch<>(levels[levelIndex], IntStream.rangeClosed(l, r).mapToObj(i -> new Pair<>(i, fr.get(i)))));
            }

            if (levelIndex != lastLevelIndex) {
                FractionalCascadingRun<K, V> fcRun = (FractionalCascadingRun<K, V>) run;
                l = fcRun.getFCHigh(keyLow);
                r = fcRun.getFCLow(keyHigh);
            }
        }
        return new RangeSearchResult<>(b.build());
    }
}
