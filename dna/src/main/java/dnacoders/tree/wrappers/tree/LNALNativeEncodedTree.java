package dnacoders.tree.wrappers.tree;

import core.BaseSequence;
import datastructures.reference.IDNASketch;
import dnacoders.tree.encodednodestorage.EncodedNodeStorage;
import dnacoders.tree.wrappers.node.*;
import java.util.function.Function;

public class LNALNativeEncodedTree<K extends Comparable<K>, V, S extends IDNASketch> extends AbstractEncodedBPTree<K, V, BaseSequence[], S> {

    public LNALNativeEncodedTree(Function<EncodedNode<S>, DecodedNode<K, S>> decoder, EncodedNodeStorage<BaseSequence[], S> encodedNodeStorage) {
        super(encodedNodeStorage, decoder);
    }

    @Override
    protected BaseSequence[] getAddress(DecodedInternalNode<K, S> internalNode, K key) {
        return internalNode.findKidAddresses(key);
    }

    @Override
    protected BaseSequence[] getAddress(DecodedInternalNode<K, S> internalNode, int index) {
        return internalNode.kidAddressesAt(index);
    }

    @Override
    protected BaseSequence[] getAddress(S sketch) {
        return sketch.addresses();
    }
}
