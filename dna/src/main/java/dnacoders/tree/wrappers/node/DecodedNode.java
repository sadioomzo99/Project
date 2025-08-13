package dnacoders.tree.wrappers.node;

import datastructures.reference.IDNASketch;
import java.util.List;

public abstract class DecodedNode<K extends Comparable<K>, S extends IDNASketch> {
    protected final List<K> keys;

    public DecodedNode(List<K> keys) {
        this.keys = keys;
    }

    public List<K> getKeys() {
        return keys;
    }

    public int size() {
        return keys.size();
    }

    public abstract boolean isLeaf();
    public DecodedInternalNode<K, S> asDecodedInternalNode() {
        return (DecodedInternalNode<K, S>) this;
    }


    public <V> DecodedLeafNode<K, V, S> asDecodedLeaf() {
        return (DecodedLeafNode<K, V, S>) this;
    }

    public DecodedInternalNodeAboveLeaf<K, S> asDecodedInternalNodeAboveLeaf() {
        return (DecodedInternalNodeAboveLeaf<K, S>) this;
    }
}
