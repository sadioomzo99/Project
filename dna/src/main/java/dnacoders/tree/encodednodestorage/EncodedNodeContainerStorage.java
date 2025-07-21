package dnacoders.tree.encodednodestorage;

import core.BaseSequence;
import datastructures.container.DNAContainer;
import datastructures.reference.IDNASketch;
import dnacoders.tree.coders.BPTreeContainerCoder;
import dnacoders.tree.wrappers.node.EncodedNode;
import utils.AddressedDNA;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

public class EncodedNodeContainerStorage implements EncodedNodeStorage<Long, IDNASketch.ContainerIdSketch> {

    private final DNAContainer container;
    private final long rootId;
    private final Collection<Long> nodeIds;

    public EncodedNodeContainerStorage(DNAContainer container, long rootId, Collection<Long> nodeIds) {
        this.container = container;
        this.rootId = rootId;
        this.nodeIds = nodeIds;
    }

    @Override
    public EncodedNode<IDNASketch.ContainerIdSketch> getRoot() {
        var oligos = container.getOligos(rootId);
        return new EncodedNode<>(
                size() == 1L,
                Arrays.stream(oligos).map(AddressedDNA::payload).toArray(BaseSequence[]::new),
                new IDNASketch.ContainerIdSketch(rootId, container),
                Arrays.stream(oligos).map(AddressedDNA::join).toArray(BaseSequence[]::new)
        );
    }

    @Override
    public boolean isParallel() {
        return true;
    }

    @Override
    public Collection<EncodedNode<IDNASketch.ContainerIdSketch>> collect() {
        return stream().toList();
    }

    @Override
    public long size() {
        return container.size();
    }

    @Override
    public EncodedNode<IDNASketch.ContainerIdSketch> findNode(Long id) {
        var oligos = container.getOligos(id);
        BaseSequence seq = container.assembleFromOligos(oligos);
        return new EncodedNode<>(
                seq.get(0) == BPTreeContainerCoder.LEAF_MARKER_BASE,
                Arrays.stream(oligos).map(AddressedDNA::payload).toArray(BaseSequence[]::new),
                new IDNASketch.ContainerIdSketch(id, container),
                Arrays.stream(oligos).map(AddressedDNA::join).toArray(BaseSequence[]::new)
        );
    }

    @Override
    public boolean isEmpty() {
        return container.size() <= 0L;
    }

    @Override
    public Iterator<EncodedNode<IDNASketch.ContainerIdSketch>> iterator() {
        return stream().iterator();
    }

    @Override
    public Stream<EncodedNode<IDNASketch.ContainerIdSketch>> stream() {
        return nodeIds.stream().map(this::findNode);
    }
}
