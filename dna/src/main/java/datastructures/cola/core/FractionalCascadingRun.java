package datastructures.cola.core;

import datastructures.cola.queryresult.StabbingSearchResult;

public interface FractionalCascadingRun<K extends Comparable<K>, V> extends Run<K, V> {
    default StabbingSearchResult<K, V> search(K key, int low, int high) {
        var sr = Run.super.search(key, low, high);
        if (sr.isMatch())
            return StabbingSearchResult.match(sr.getResult(), sr.getIndex(), level(), getFCLow(key), getFCHigh(key));

        return StabbingSearchResult.noMatch(sr.getIndex(), level(), getFCLow(key), getFCHigh(key));
    }

    void updateFC(Run<K, V> target);
    int getFCLow(K key);
    int getFCHigh(K key);
}
