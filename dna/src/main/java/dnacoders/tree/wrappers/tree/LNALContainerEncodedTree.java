package dnacoders.tree.wrappers.tree;

import datastructures.reference.IDNASketch;
import dnacoders.tree.encodednodestorage.EncodedNodeStorage;
import dnacoders.tree.wrappers.node.*;
import java.util.function.Function;


public class LNALContainerEncodedTree<K extends Comparable<K>, V> extends AbstractEncodedBPTree<K, V, Long, IDNASketch.ContainerIdSketch> {
    public LNALContainerEncodedTree(EncodedNodeStorage<Long, IDNASketch.ContainerIdSketch> encodedNodeStorage, Function<EncodedNode<IDNASketch.ContainerIdSketch>, DecodedNode<K, IDNASketch.ContainerIdSketch>> decoder) {
        super(encodedNodeStorage, decoder);
    }

    @Override
    protected Long getAddress(DecodedInternalNode<K, IDNASketch.ContainerIdSketch> internalNode, K key) {
        return internalNode.findKidSketch(key).id();
    }

    @Override
    protected Long getAddress(DecodedInternalNode<K, IDNASketch.ContainerIdSketch> internalNode, int index) {
        return internalNode.kidSketchesAt(index).id();
    }

    @Override
    protected Long getAddress(IDNASketch.ContainerIdSketch sketch) {
        return sketch.id();
    }
}
