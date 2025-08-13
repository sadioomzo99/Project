package dnacoders.tree.sketchers;

import core.BaseSequence;
import datastructures.reference.IDNASketch;
import utils.AsymmetricCoder;
import utils.lsh.LSH;
import java.util.function.Function;

public interface IDNASketcher<S extends IDNASketch> extends Function<S, BaseSequence[]> {

    BaseSequence[] generateFromSketch(S s);
    S createSketch(int n, int tol);

    int addressSize();

    @Override
    default BaseSequence[] apply(S s) {
        return generateFromSketch(s);
    }

    AsymmetricCoder<S, DecodedSketch<S>, BaseSequence> coder();
    LSH<BaseSequence> getLSH();

    default BaseSequence encodeSketch(S sketch) {
        return coder().encode(sketch);
    }

    default DecodedSketch<S> decodeSketch(BaseSequence encodedSketch) {
        return coder().decode(encodedSketch);
    }

    record DecodedSketch<S extends IDNASketch>(S sketch, int serializedSize) {

    }
}
