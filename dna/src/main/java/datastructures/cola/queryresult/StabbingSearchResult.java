package datastructures.cola.queryresult;

import datastructures.KVEntry;

public class StabbingSearchResult<K extends Comparable<K>, V> {
    private final KVEntry<K, V> result;
    private final int index;
    private final int level;
    private final int indexLow;
    private final int indexHigh;

    private final boolean matching;


    private StabbingSearchResult(KVEntry<K, V> result, boolean matching, int index, int level, int indexLow, int indexHigh) {
        this.result = result;
        this.matching = matching;
        this.index = index;
        this.indexLow = indexLow;
        this.indexHigh = indexHigh;
        this.level = level;
    }

    public static <K extends Comparable<K>, V> StabbingSearchResult<K, V> match(KVEntry<K, V> result, int index, int level) {
        return new StabbingSearchResult<>(result, true, index, level, -1, -1);
    }

    public static <K extends Comparable<K>, V> StabbingSearchResult<K, V> match(KVEntry<K, V> result, int index, int level, int indexLow, int indexHigh) {
        return new StabbingSearchResult<>(result, true, index, level, indexLow, indexHigh);
    }

    public static <K extends Comparable<K>, V> StabbingSearchResult<K, V> noMatch(int expectedIndex, int currentLevel, int indexLow, int indexHigh) {
        return new StabbingSearchResult<>(null, false, expectedIndex, currentLevel, indexLow, indexHigh);
    }

    public static <K extends Comparable<K>, V> StabbingSearchResult<K, V> noMatch(int expectedIndex) {
        return new StabbingSearchResult<>(null, false, expectedIndex, -1, -1, -1);
    }

    public boolean getMatching() {
        return matching;
    }

    public int getIndex() {
        return index;
    }

    public int getIndexLow() {
        return indexLow;
    }

    public int getIndexHigh() {
        return indexHigh;
    }

    public KVEntry<K, V> getResult() {
        return result;
    }

    public int getLevel() {
        return level;
    }

    public boolean isMatch() {
        return this.matching;
    }

    public boolean isNoMatch() {
        return !this.matching;
    }

    @Override
    public String toString() {
        if (isMatch()) {
            return "SearchResult{" +
                   "MATCH" +
                    ", level=" + level +
                    ", entry=" + result +
                    ", index=" + index +
                    '}';
        }
        return "SearchResult{" +
                "NO MATCH}";

    }

    public enum Matching {
        MATCH, NO_MATCH
    }
}
