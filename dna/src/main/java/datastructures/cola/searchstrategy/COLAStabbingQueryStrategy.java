package datastructures.cola.searchstrategy;

import datastructures.cola.COLA;
import datastructures.cola.core.Run;
import datastructures.cola.queryresult.StabbingSearchResult;
import java.util.Arrays;

public interface COLAStabbingQueryStrategy<K extends Comparable<K>, V> {
    StabbingSearchResult<K, V> search(K key, COLA<K, V> cola);


    static<K extends Comparable<K>, V> StabbingSearchResult<K, V> bottomUp(K key, COLA<K, V> cola) {
        return searchLevels(key, cola, cola.getBusyLevelsBottomUp());
    }

    static<K extends Comparable<K>, V> StabbingSearchResult<K, V> topDown(K key, COLA<K, V> cola) {
        return searchLevels(key, cola, cola.getBusyLevelsTopDown());
    }

    private static<K extends Comparable<K>, V> StabbingSearchResult<K, V> searchLevels(K key, COLA<K, V> cola, int[] indexes) {
        return Arrays.stream(indexes)
                .mapToObj(cola::getRun)
                .map(run -> run.search(key))
                .filter(StabbingSearchResult::isMatch)
                .findFirst()
                .orElse(StabbingSearchResult.noMatch(-1));
    }

    static<K extends Comparable<K>, V> StabbingSearchResult<K, V> fractionalCascade(K key, COLA<K, V> cola) {
        int[] levels = cola.getBusyLevelsTopDown();
        if (levels.length == 0)
            return StabbingSearchResult.noMatch(-1);

        Run<K, V> currentRun = cola.getRun(levels[0]);
        StabbingSearchResult<K, V> result = currentRun.search(key);
        if (result.isMatch())
            return result;
        int low = result.getIndexLow();
        int high = result.getIndexHigh();
        for (int i = 1; i < levels.length; i++) {
            currentRun = cola.getRun(levels[i]);
            if (low != -1)
                result = currentRun.search(key, low, high == -1 ? currentRun.size() - 1 : high);
            else if (high != -1)
                result = currentRun.search(key, low, high);

            if (result.isMatch())
                break;

            low = result.getIndexLow();
            high = result.getIndexHigh();
        }
        return result;
    }
}
