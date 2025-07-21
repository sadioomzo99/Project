package dnacoders.tree.wrappers.node;

import core.BaseSequence;
import datastructures.reference.IDNASketch;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DecodedInternalNode<K extends Comparable<K>, S extends IDNASketch> extends DecodedNode<K, S> {
    protected final List<S> kidsSketches;

    public DecodedInternalNode(List<K> keys, S[] kidsSketches) {
        this(keys, Arrays.asList(kidsSketches));
    }

    public DecodedInternalNode(List<K> keys, List<S> kidsSketches) {
        super(keys);
        this.kidsSketches = kidsSketches;
    }

    public BaseSequence[][] kidsAddresses() {
        return kidsSketches.stream().map(IDNASketch::addresses).toArray(BaseSequence[][]::new);
    }

    public List<S> kidsSketches() {
        return kidsSketches;
    }

    public S kidSketchesAt(int i) {
        return kidsSketches.get(i);
    }

    public BaseSequence[] kidAddressesAt(int i) {
        return kidsSketches.get(i).addresses();
    }

    public BaseSequence[] findKidAddresses(K key) {
        return findKidSketch(key).addresses();
    }

    public S findKidSketch(K key) {
        return kidsSketches.get(findKidIndex(key));
    }

    public int size() {
        return kidsSketches.size();
    }

    public int findKidIndex(K key) {
        int index = Collections.binarySearch(keys, key);
        return index < 0 ? -index - 1 : index;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }
    public boolean isAboveLeaf()  {
        return false;
    }

    @Override
    public String toString() {
        return "keys=" + keys + ", kidsSketches= #" + (kidsSketches == null? 0 : kidsSketches.size());
    }

}
