package datastructures.cola.core;

import datastructures.KVEntry;
import utils.FuncUtils;
import java.util.Iterator;
import java.util.stream.Stream;

public interface RunBuilder<K extends Comparable<K>, V> {
    void add(KVEntry<K, V> entry);
    Run<K, V> build();

    default void addAll(Run<K, V> run) {
        FuncUtils.stream(run::bufferedIterator).forEach(this::add);
    }

    default void addAll(Iterator<KVEntry<K, V>> it) {
        it.forEachRemaining(this::add);
    }

    default void addAll(Iterable<KVEntry<K, V>> iterable) {
        iterable.forEach(this::add);
    }

    default void addAll(Stream<KVEntry<K, V>> stream) {
        stream.forEach(this::add);
    }

    abstract class AbstractRunBuilder<K extends Comparable<K>, V> implements RunBuilder<K, V> {
        protected int size;
        protected int level;

        public AbstractRunBuilder(int level) {
            this(0, level);
        }

        public AbstractRunBuilder(int size, int level) {
            this.size = size;
            this.level = level;
        }

        public int getSize() {
            return size;
        }

        public int getLevel() {
            return level;
        }
    }
}
