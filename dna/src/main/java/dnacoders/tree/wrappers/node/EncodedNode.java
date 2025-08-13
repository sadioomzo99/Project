package dnacoders.tree.wrappers.node;

import core.BaseSequence;
import datastructures.reference.IDNAFedReference;
import datastructures.reference.IDNAReference;
import datastructures.reference.IDNASketch;
import dnacoders.tree.coders.BPTreeAsymmetricCoder;
import dnacoders.tree.sketchers.IDNASketcher;
import utils.FuncUtils;
import java.util.Arrays;
import java.util.function.Function;

public class EncodedNode<S extends IDNASketch> extends IDNAReference.DNAReference<S> {
    private final boolean isLeaf;
    private final BaseSequence[] oligos;

    public EncodedNode(boolean isLeaf, BaseSequence[] payloads, S sketch, BaseSequence[] oligos) {
        super(sketch, payloads);
        this.isLeaf = isLeaf;
        this.oligos = oligos;
    }

    public static <S extends IDNASketch> EncodedNode<S> of(boolean isLeaf, IDNASketcher<S> sketcher, Function<Integer, Integer> tolerance, BaseSequence[] payloads) {
        S sketch = sketcher.createSketch(payloads.length, tolerance.apply(payloads.length));
        return new EncodedNode<>(
                isLeaf,
                payloads,
                sketch,
                FuncUtils.zip(Arrays.stream(sketch.addresses()), Arrays.stream(payloads), BaseSequence::join).toArray(BaseSequence[]::new)
        );
    }

    @Override
    public int size() {
        return payloads().length;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    @Override
    public BaseSequence[] joinedOligos() {
        return oligos;
    }

    public <T> IDNAFedReference<T, S> asFedReference(Function<EncodedNode<S>, T> decoder) {
        return IDNAFedReference.of(this, decoder);
    }

    public <K extends Comparable<K>, V> IDNAFedReference<DecodedNode<K, S>, S> asFedReference(BPTreeAsymmetricCoder<K, V, S, ?> coder) {
        return asFedReference(coder::decodeNode);
    }

    public <K extends Comparable<K>, V> DecodedNode<K, S> decode(BPTreeAsymmetricCoder<K, V, S, ?> coder) {
        return asFedReference(coder::decodeNode).decode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        EncodedNode<?> that = (EncodedNode<?>) o;
        return isLeaf == that.isLeaf && Arrays.equals(oligos, that.oligos);
    }

    @Override
    public String toString() {
        return "leaf: " + isLeaf + ", size: " + payloads().length;
    }
}

