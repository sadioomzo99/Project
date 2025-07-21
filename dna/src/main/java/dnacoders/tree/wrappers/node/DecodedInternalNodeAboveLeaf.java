package dnacoders.tree.wrappers.node;

import datastructures.reference.IDNASketch;
import java.util.List;

public class DecodedInternalNodeAboveLeaf<K extends Comparable<K>, S extends IDNASketch> extends DecodedInternalNode<K, S> {
    private final S rightSketch;

    public DecodedInternalNodeAboveLeaf(List<K> keys, S[] kidsSketches, S rightSketch) {
        super(keys, kidsSketches);
        this.rightSketch = rightSketch;
    }

    public DecodedInternalNodeAboveLeaf(List<K> keys, List<S> kidsSketches, S rightSketch) {
        super(keys, kidsSketches);
        this.rightSketch = rightSketch;
    }

    public S getRightSketch() {
        return rightSketch;
    }

    @Override
    public boolean isAboveLeaf()  {
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + ", rightSketch=" + (rightSketch == null ? null : "pointer");
    }
}
