package dnacoders.tree.wrappers.tree;

import datastructures.reference.IDNASketch;
import datastructures.searchtrees.BPTreeQuery;
import dnacoders.tree.encodednodestorage.EncodedNodeStorage;
import dnacoders.tree.wrappers.node.DecodedNode;
import dnacoders.tree.wrappers.node.EncodedNode;
import utils.FuncUtils;
import utils.Streamable;
import java.util.*;
import java.util.stream.Stream;

public interface EncodedBPTree<K extends Comparable<K>, V, ADDR, S extends IDNASketch> extends BPTreeQuery<K, V>, Streamable<EncodedNode<S>> {

    EncodedNode<S> getRoot();
    EncodedNodeStorage<ADDR, S> getEncodedNodeStorage();
    DecodedNode<K, S> decode(EncodedNode<S> en);
    Iterator<EncodedNode<S>> searchLeafIterator(K key);

    @Override
    default Iterator<EncodedNode<S>> iterator() {
        return stream().iterator();
    }

    @Override
    default Stream<EncodedNode<S>> stream() {
        return getEncodedNodeStorage().stream();
    }

    default long size() {
        return getEncodedNodeStorage().size();
    }

    default boolean isEmpty() {
        return getEncodedNodeStorage().isEmpty();
    }

    default Stream<EncodedNode<S>> searchLeafStream(K key) {
        return FuncUtils.stream(() -> searchLeafIterator(key));
    }

    default Optional<EncodedNode<S>> searchLeaf(K key) {
        return FuncUtils.tryOrElse(
                () -> Optional.of(searchLeafIterator(key).next()),
                () -> Optional.empty()
        );
    }
}
