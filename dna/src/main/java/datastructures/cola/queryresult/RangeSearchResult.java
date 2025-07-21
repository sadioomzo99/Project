package datastructures.cola.queryresult;

import datastructures.KVEntry;
import utils.Pair;
import utils.Streamable;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

public class RangeSearchResult<K extends Comparable<K>, V> implements Streamable<Pair<Integer, KVEntry<K, V>>> {
    private final Stream<ResultPatch<K, V>> matches;

    public RangeSearchResult(Stream<ResultPatch<K, V>> matches) {
        this.matches = matches;
    }

    public static <K extends Comparable<K>, V> RangeSearchResult<K, V> empty() {
        return new RangeSearchResult<>(Stream.empty());
    }

    @Override
    public Iterator<Pair<Integer, KVEntry<K, V>>> iterator() {
        return stream().iterator();
    }

    @Override
    public Stream<Pair<Integer, KVEntry<K, V>>> stream() {
        return matches.flatMap(ResultPatch::stream);
    }

    public Stream<ResultPatch<K, V>> streamPatches() {
        return matches;
    }

    @Override
    public String toString() {
        var collected = matches.flatMap(m -> m.entries).map(Objects::toString).toArray(String[]::new);
        if (collected.length > 0)
            return "RangeSearchResult{size=" + collected.length + ", [" + String.join(", ", collected) + "]}";
        return "RangeSearchResult{NO MATCH}";
    }

    public static class ResultPatch<K extends Comparable<K>, V> implements Streamable<Pair<Integer, KVEntry<K, V>>> {
        private final int level;
        private final Stream<Pair<Integer, KVEntry<K, V>>> entries;

        public ResultPatch(int level, Stream<Pair<Integer, KVEntry<K, V>>> entries) {
            this.level = level;
            this.entries = entries;
        }

        @Override
        public Stream<Pair<Integer, KVEntry<K, V>>>stream() {
            return entries;
        }

        @Override
        public Iterator<Pair<Integer, KVEntry<K, V>>> iterator() {
            return entries.iterator();
        }


        public int getLevel() {
            return level;
        }
    }
}
