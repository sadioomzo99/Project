package datastructures.cola;

import datastructures.KVEntry;
import datastructures.cola.core.Run;
import datastructures.cola.queryresult.RangeSearchResult;
import datastructures.cola.queryresult.StabbingSearchResult;
import datastructures.cola.run.managers.RunsManager;
import datastructures.cola.searchstrategy.COLARangeQueryStrategy;
import datastructures.cola.searchstrategy.COLAStabbingQueryStrategy;
import utils.Streamable;
import java.util.Iterator;

public interface COLA<K extends Comparable<K>, V> extends Streamable<KVEntry<K, V>> {
    long size();
    void insert(K key, V value);

    default Run<K, V> getRun(int level) {
        return getRunsManager().get(level);
    }

    default StabbingSearchResult<K, V> search(K key, COLAStabbingQueryStrategy<K, V> searchStrategy) {
        return searchStrategy.search(key, this);
    }
    default StabbingSearchResult<K, V> search(K key) {
        return COLAStabbingQueryStrategy.topDown(key, this);
    }

    default  RangeSearchResult<K, V> searchRange(K keyLow, K keyHigh, COLARangeQueryStrategy<K, V> searchStrategy) {
        return searchStrategy.search(keyLow, keyHigh, this);
    }
    default  RangeSearchResult<K, V> searchRange(K keyLow, K keyHigh) {
        return COLARangeQueryStrategy.topDown(keyLow, keyHigh, this);
    }

    RunsManager<K, V> getRunsManager();

    default int[] getBusyLevelsTopDown() {
        return getRunsManager().getBusyLevelsTopDown();
    }

    default int[] getBusyLevelsBottomUp() {
        return getRunsManager().getBusyLevelsBottomUp();
    }

    default boolean isLevelFull(int level) {
        return getRunsManager().isLevelFull(level);
    }

    @Override
    default Iterator<KVEntry<K, V>> iterator() {
        return getRunsManager().stream().flatMap(Streamable::stream).iterator();
    }
}
