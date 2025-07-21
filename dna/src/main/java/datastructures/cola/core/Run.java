package datastructures.cola.core;

import datastructures.KVEntry;
import datastructures.cola.queryresult.RangeSearchResult;
import datastructures.cola.queryresult.StabbingSearchResult;
import datastructures.cola.run.MemRun;
import utils.BufferedIterator;
import utils.FuncUtils;
import utils.Pair;
import utils.Streamable;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface Run<K extends Comparable<K>, V> extends Streamable<KVEntry<K, V>> {

    int DEFAULT_NUM_BUFFED_ELEMENTS = 256;

    KVEntry<K, V> get(int index);
    K getKey(int index);
    V getValue(int index);
    int size();
    int level();

    default Run<K, V> merge(Run<K, V> run, RunBuilder<K, V> runBuilder) {
        return merge(this, run, runBuilder);
    }
    default Iterator<KVEntry<K, V>> iterator() {
        return IntStream.range(0, size()).mapToObj(this::get).iterator();
    }

    default Run<K, V> toMemRun() {
        MemRun.Builder<K, V> b = new MemRun.Builder<>(size(), level());
        b.addAll(bufferedIterator());
        return b.build();
    }

    default Stream<KVEntry<K, V>> parallelStream() {
        ExecutorService pool = Executors.newWorkStealingPool();
        List<Future<KVEntry<K, V>>> futures = IntStream.range(0, size()).mapToObj(i -> pool.submit(() -> this.get(i))).toList();
        pool.close();
        return futures.stream().map(f -> FuncUtils.safeCall(f::get));
    }

    default BufferedIterator<KVEntry<K, V>> bufferedIterator(int numBuffedElements) {
        return BufferedIterator.of(iterator(), numBuffedElements);
    }

    default BufferedIterator<KVEntry<K, V>> bufferedIterator() {
        return bufferedIterator(DEFAULT_NUM_BUFFED_ELEMENTS);
    }

    default StabbingSearchResult<K, V> search(K key) {
        return search(key, 0, size() - 1);
    }

    default StabbingSearchResult<K, V> search(K key, int low, int high) {
        int mid;
        while (low <= high) {
            mid = (low + high) >>> 1;
            K midKey = getKey(mid);
            int cmp = midKey.compareTo(key);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return StabbingSearchResult.match(new KVEntry<>(midKey, getValue(mid)), mid, level());
        }

        return StabbingSearchResult.noMatch(low);
    }

    default RangeSearchResult.ResultPatch<K, V> searchRange(K keyLow, K keyHigh, int low, int high) {
        StabbingSearchResult<K, V> left = search(keyLow);
        int size = size();
        int leftIndex = left.getIndex();
        if (leftIndex >= low && leftIndex < size) {
            StabbingSearchResult<K, V> right = search(keyHigh);
            int rightIndex = right.getIndex();
            if (right.isNoMatch())
                rightIndex--;

            if (rightIndex <= high && leftIndex <= rightIndex)
                return new RangeSearchResult.ResultPatch<>(level(), IntStream.rangeClosed(leftIndex, rightIndex).mapToObj(i -> new Pair<>(i, get(i))));
        }
        Stream<Pair<Integer, KVEntry<K, V>>> es = Stream.empty();
        return new RangeSearchResult.ResultPatch<>(-1, es);
    }

    default RangeSearchResult.ResultPatch<K, V> searchRange(K keyLow, K keyHigh) {
        return searchRange(keyLow, keyHigh, 0, size() - 1);
    }

    default void deallocate() {

    }

    static <K extends Comparable<K>, V> Run<K, V> merge(Run<K, V> r1, Run<K, V> r2, RunBuilder<K, V> runBuilder) {
        int size = r1.size();
        if (size != r2.size())
            throw new RuntimeException("invalid merge! this.size=" + size + " != that.size=" + r2.size());

        BufferedIterator<KVEntry<K, V>> it1 = r1.bufferedIterator();
        BufferedIterator<KVEntry<K, V>> it2 = r2.bufferedIterator();
        KVEntry<K, V> e1 = it1.peek();
        KVEntry<K, V> e2 = null;
        boolean it1PointerMoved = false;
        do {
            if (!it1.hasNext()) {
                it2.forEachRemaining(runBuilder::add);
                break;
            }
            if (!it2.hasNext()) {
                it1.forEachRemaining(runBuilder::add);
                break;
            }
            if (it1PointerMoved)
                e1 = it1.peek();
            else
                e2 = it2.peek();

            if (e1.compareTo(e2) <= 0) {
                runBuilder.add(e1);
                it1PointerMoved = true;
                it1.advance();
            }
            else {
                runBuilder.add(e2);
                it1PointerMoved = false;
                it2.advance();
            }
        } while (true);

        return runBuilder.build();
    }


    abstract class AbstractRun<K extends Comparable<K>, V> implements Run<K, V> {
        protected int size;
        protected int level;

        public AbstractRun(int size, int level) {
            this.size = size;
            this.level = level;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public int level() {
            return level;
        }

        public String toString() {
            return getClass().getSimpleName() + "{"
                    + "size=" + size() + ", level=" + level() + ", entries=["
                    + FuncUtils.stream(this::bufferedIterator).map(Objects::toString).collect(Collectors.joining(","))
                    + "]}";
        }
    }
}
