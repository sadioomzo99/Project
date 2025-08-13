package datastructures.hashtable;

import utils.Streamable;
import java.util.AbstractCollection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HashTable<T> implements Streamable<HashSet<T>> {
    private final List<HashSet<T>> table;
    private final IntHashFunction<T> intHashFunction;

    public HashTable(int numBuckets) {
        this(numBuckets, IntHashFunction.hashCodeMapping());
    }

    public HashTable(List<HashSet<T>> table) {
        this(table, IntHashFunction.hashCodeMapping());
    }

    public HashTable(int numBuckets, IntHashFunction<T> intHashFunction) {
        this(IntStream.range(0, numBuckets).mapToObj(i -> new HashSet<T>()).toList(), intHashFunction);
    }

    public HashTable(List<HashSet<T>> table, IntHashFunction<T> intHashFunction) {
        this.table = table;
        this.intHashFunction = intHashFunction;
    }

    public void put(T t) {
        putIntoBucket(getHashedBucketId(t), t);
    }

    public void putIntoBucket(int bucketId, T t) {
        table.get(bucketId).add(t);
    }

    public int getHashedBucketId(T t) {
        return intHashFunction.applyMod(t, size());
    }

    public HashSet<T> getBucket(int n) {
        return table.get(n);
    }

    public int size() {
        return table.size();
    }

    @Override
    public Iterator<HashSet<T>> iterator() {
        return table.iterator();
    }

    @Override
    public String toString() {
        return table.stream().map(AbstractCollection::toString).collect(Collectors.joining("\n"));
    }
}
