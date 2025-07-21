package dnacoders.tree.sketchers;

import core.Attribute;
import core.BaseSequence;
import core.dnarules.DNARule;
import datastructures.reference.IDNASketch;
import utils.Coder;
import utils.FuncUtils;
import utils.lsh.LSH;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DNASketcherV1 extends AbstractHashSketcher {

    private static final String ATTRIBUTE_NAME = "id";
    private static final Coder<Long, Attribute<String>> LONG_TO_ATTRIBUTE_CODER = Coder.of(l -> new Attribute<>(ATTRIBUTE_NAME, String.valueOf(l)), a -> Long.parseLong(a.getValue()));

    private final Function<Long, BaseSequence> address0Generator;

    public DNASketcherV1(int addressSize, LSH<BaseSequence> lsh, DNARule errorRules, Coder<String, BaseSequence> stringCoder, boolean parallel, boolean manageLsh, int addressErrorOpts, float errorWeight, float distanceWeight) {
        super(addressSize, lsh, errorRules, stringCoder, parallel, manageLsh, addressErrorOpts, errorWeight, distanceWeight);
        var padder = PADDER_FUNC.apply(addressSize);
        this.address0Generator = l -> {
            var seq = padder.encode(attributeCoder.encode(LONG_TO_ATTRIBUTE_CODER.encode(l)));
            return limitToAddressSize(seq);
        };
    }

    @Override
    protected Stream<BaseSequence> generateCandidates(int total, long seed) {
        BaseSequence addr0 = address0Generator.apply(seed);
        return FuncUtils.stream(IntStream.iterate(0, i -> i < total, i -> i + 1), parallel).mapToObj(i -> computeAddr(addr0, i));
    }

    private BaseSequence computeAddr(BaseSequence addr0, int i) {
        List<BaseSequence> candidates = new ArrayList<>(addressErrorOpts);
        BaseSequence iAddr = addr0.permute(getPermutation(i));
        candidates.add(iAddr);

        int trials = addressErrorOpts - 1;
        for (int j = 0; j < trials; j++)
            candidates.add(iAddr = iAddr.permute(getPermutation(j)));


        return candidates.stream()
                .peek(seq -> seq.putProperty(ERROR_PROPERTY_NAME, errorScore(seq)))
                .min(Comparator.comparing(seq -> seq.getProperty(ERROR_PROPERTY_NAME)))
                .orElseThrow();
    }

    @Override
    public BaseSequence[] generateFromSketch(IDNASketch.HashSketch hashSketch) {
        BaseSequence addr0 = address0Generator.apply(hashSketch.seed());
        return usedIndices(hashSketch.n(), hashSketch.badIndices())
                .mapToObj(id -> computeAddr(addr0, id))
                .toArray(BaseSequence[]::new);
    }
}
