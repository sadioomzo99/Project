package dnacoders.tree.wrappers.tree;

import datastructures.reference.IDNASketch;
import dnacoders.tree.encodednodestorage.EncodedNodeStorage;
import dnacoders.tree.wrappers.node.*;
import utils.FuncUtils;
import utils.Tuple3;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class AbstractEncodedBPTree<K extends Comparable<K>,  V, ADDR, S extends IDNASketch> implements EncodedBPTree<K, V, ADDR, S> {
    protected final EncodedNodeStorage<ADDR, S> encodedNodeStorage;
    protected final Function<EncodedNode<S>, DecodedNode<K, S>> decoder;

    public AbstractEncodedBPTree(EncodedNodeStorage<ADDR, S> encodedNodeStorage, Function<EncodedNode<S>, DecodedNode<K, S>> decoder) {
        this.encodedNodeStorage = encodedNodeStorage;
        this.decoder = decoder;
    }

    protected abstract ADDR getAddress(DecodedInternalNode<K, S> internalNode, K key);
    protected abstract ADDR getAddress(DecodedInternalNode<K, S> internalNode, int index);
    protected abstract ADDR getAddress(S sketch);

    @Override
    public DecodedNode<K, S> decode(EncodedNode<S> en) {
        return decoder.apply(en);
    }
    @Override
    public EncodedNode<S> getRoot() {
        return encodedNodeStorage.getRoot();
    }
    @Override
    public EncodedNodeStorage<ADDR, S> getEncodedNodeStorage() {
        return encodedNodeStorage;
    }


    @Override
    public V search(K key) {
        var root = getRoot();
        if (root == null)
            return null;

        DecodedNode<K, S> node = decode(root);
        EncodedNodeStorage<ADDR, S> storage = getEncodedNodeStorage();
        while (!node.isLeaf()) {
            ADDR nextAddrs = getAddress(node.asDecodedInternalNode(), key);
            node = decode(storage.findNode(nextAddrs));
        }

        DecodedLeafNode<K, V, S> leaf = node.asDecodedLeaf();
        int index = Collections.binarySearch(leaf.getKeys(), key);
        return index >= 0 ? leaf.getValues().get(index) : null;
    }

    @Override
    public Stream<V> search(K keyLow, K keyHigh) {
        AtomicBoolean flag = new AtomicBoolean(false);
        return FuncUtils.stream(() -> searchLeafIterator(keyLow))
                .map(en -> ((DecodedLeafNode<K, V, S>) decode(en)))
                .map(leaf -> leaf.searchByKey(keyLow, keyHigh))
                .takeWhile(result -> {
                    if (flag.get())
                        return false;

                    if (result.isLastLeaf())
                        flag.set(true);

                    return true;
                })
                .map(DecodedLeafNode.RangeSearchResult::hits)
                .flatMap(Collection::stream);
    }

    @Override
    public Iterator<EncodedNode<S>> searchLeafIterator(K key) {
        if (isEmpty())
            return Collections.emptyIterator();

        return new Iterator<>() {
            Tuple3<Integer, EncodedNode<S>, DecodedInternalNode<K, S>> n;
            EncodedNode<S> leaf;
            DecodedInternalNode<K, S> decodedParent;
            int nextIndex;

            @Override
            public boolean hasNext() {
                if (n == null) {
                    n = searchLeafIndexWithParent(key);
                    decodedParent = n.t3();
                    leaf = n.t2();
                    nextIndex = n.t1();
                }
                return decodedParent != null && (nextIndex < decodedParent.size() || rightHash(decodedParent) != null);
            }

            @Override
            public EncodedNode<S> next() {
                if (leaf != null) {
                    var l = leaf;
                    leaf = null;
                    nextIndex++;
                    return l;
                }
                if (nextIndex < decodedParent.size())
                    return encodedNodeStorage.findNode(getAddress(decodedParent, nextIndex++));

                decodedParent = decode(encodedNodeStorage.findNode(getAddress(rightHash(decodedParent)))).asDecodedInternalNode();
                nextIndex = 1;
                return encodedNodeStorage.findNode(getAddress(decodedParent, 0));
            }

            private static <K extends Comparable<K>, S extends IDNASketch> S rightHash(DecodedInternalNode<K, S> node) {
                if (node instanceof DecodedInternalNodeAboveLeaf<K, S> n)
                    return n.getRightSketch();

                return null;
            }
        };
    }

    private Tuple3<Integer, EncodedNode<S>, DecodedInternalNode<K, S>> searchLeafIndexWithParent(K key) {
        EncodedNode<S> hit = getRoot();
        DecodedNode<K, S> parent = null;
        int index = -1;
        while (!hit.isLeaf()) {
            parent = decode(hit);
            DecodedInternalNode<K, S> din = parent.asDecodedInternalNode();
            index = din.findKidIndex(key);
            ADDR nextAddrs = getAddress(din, index);
            hit = encodedNodeStorage.findNode(nextAddrs);
        }

        return new Tuple3<>(
                index,
                hit,
                parent != null ? parent.asDecodedInternalNode() : null
        );
    }
}
