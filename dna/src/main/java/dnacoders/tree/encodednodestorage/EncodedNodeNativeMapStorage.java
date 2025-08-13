package dnacoders.tree.encodednodestorage;

import core.BaseSequence;
import datastructures.reference.IDNASketch;
import dnacoders.tree.wrappers.node.EncodedNode;
import utils.FuncUtils;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class EncodedNodeNativeMapStorage<S extends IDNASketch> implements EncodedNodeStorage<BaseSequence[], S> {

    private final List<EncodedNode<S>> nodes;
    private final boolean isParallel;
    private final EncodedNode<S> root;

    public EncodedNodeNativeMapStorage(List<EncodedNode<S>> nodes, EncodedNode<S> root, boolean isParallel) {
        this.nodes = nodes;
        this.isParallel = isParallel;
        this.root = root;
    }

    @Override
    public long size() {
        return nodes.size();
    }
    @Override
    public boolean isParallel() {
        return isParallel;
    }
    @Override
    public EncodedNode<S> findNode(BaseSequence[] addresses) {
        return FuncUtils.stream(nodes.stream(), isParallel).filter(en -> Arrays.equals(en.addresses(), addresses)).findFirst().orElse(null);
    }
    @Override
    public Collection<EncodedNode<S>> collect() {
        return nodes;
    }
    @Override
    public EncodedNode<S> getRoot() {
        return root;
    }
    @Override
    public Iterator<EncodedNode<S>> iterator() {
        return nodes.iterator();
    }
    @Override
    public Stream<EncodedNode<S>> stream() {
        return nodes.stream();
    }
    @Override
    public boolean isEmpty() {
        return nodes.isEmpty();
    }
}
