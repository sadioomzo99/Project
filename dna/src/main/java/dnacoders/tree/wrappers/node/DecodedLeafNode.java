package dnacoders.tree.wrappers.node;

import datastructures.reference.IDNASketch;
import utils.FuncUtils;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DecodedLeafNode<K extends Comparable<K>, V, S extends IDNASketch> extends DecodedNode<K, S> {
    protected final List<V> values;

    public DecodedLeafNode(List<K> keys, List<V> values) {
        super(keys);
        this.values = values;
    }

    public List<V> getValues() {
        return values;
    }

    public RangeSearchResult<V> searchByKey(K keyLow, K keyHigh) {
        int size = size();
        int startIndex = IntStream.range(0, size).filter(i -> keys.get(i).compareTo(keyLow) >= 0).findFirst().orElse(-1);
        if (startIndex < 0)
            return new RangeSearchResult<>(0, 0, false, Collections.emptyList());

        int endIndex = IntStream.range(startIndex, size).filter(i -> keys.get(i).compareTo(keyHigh) > 0).findFirst().orElse(-1);
        if (endIndex < 0)
            return new RangeSearchResult<>(startIndex, size, false, values.subList(startIndex, size));

        return new RangeSearchResult<>(startIndex, endIndex, true, values.subList(startIndex, endIndex));
    }

    public List<V> searchValuesByKey(K keyLow, K keyHigh) {
        var result = searchByKey(keyLow, keyHigh);
        return values.subList(result.startInclusive, result.exclusiveEnd);
    }

    public List<V> searchByKey(Predicate<K> keyPredicate) {
        return IntStream.range(0, keys.size()).filter(i -> keyPredicate.test(keys.get(i))).mapToObj(values::get).toList();
    }

    public List<V> searchByValue(Predicate<V> keyPredicate) {
        return values.stream().filter(keyPredicate).toList();
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public String toString() {
        return "key-values={" + FuncUtils.zip(keys.stream(), values.stream()).map(Objects::toString).collect(Collectors.joining(", "));
    }

    public record RangeSearchResult<V>(int startInclusive, int exclusiveEnd, boolean isLastLeaf, List<V> hits) {

    }
}
