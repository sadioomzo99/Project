package dnacoders.tree.sketchers;

import core.Attribute;
import core.BaseSequence;
import core.dnarules.DNARule;
import datastructures.reference.IDNASketch;
import utils.Coder;
import utils.FuncUtils;
import utils.lsh.LSH;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DNASketcherV2 extends AbstractHashSketcher {

    private static final String ATTRIBUTE_NAME                 = "id";
    private static final String ATTRIBUTE_VALUE_SEPARATOR      = ".";
    private static final BiFunction<Long, Integer, Attribute<String>> ATTRIBUTE_MAPPER = (seed, n) -> new Attribute<>(ATTRIBUTE_NAME, seed + ATTRIBUTE_VALUE_SEPARATOR + n);

    private final BiFunction<Long, Integer, BaseSequence> addressGenerator;

    public DNASketcherV2(int addressSize, LSH<BaseSequence> lsh, DNARule errorRules, Coder<String, BaseSequence> stringCoder, boolean parallel, boolean manageLsh, int addressErrorOpts, float errorWeight, float distanceWeight) {
        super(addressSize, lsh, errorRules, stringCoder, parallel, manageLsh, addressErrorOpts, errorWeight, distanceWeight);
        var padder = PADDER_FUNC.apply(addressSize);
        this.addressGenerator = (seed, n) -> {
            var seq = padder.encode(attributeCoder.encode(ATTRIBUTE_MAPPER.apply(seed, n)));
            return limitToAddressSize(seq);
        };
    }

    @Override
    protected Stream<BaseSequence> generateCandidates(int total, long seed) {
        return FuncUtils.stream(IntStream.iterate(0, i -> i < total, i -> i + 1), parallel)
                .mapToObj(i -> addressGenerator.apply(seed, i))
                .map(this::optimizeError);
    }

    @Override
    public BaseSequence[] generateFromSketch(IDNASketch.HashSketch hashSketch) {
        long seed = hashSketch.seed();
        return usedIndices(hashSketch.n(), hashSketch.badIndices())
                .mapToObj(i -> addressGenerator.apply(seed, i))
                .map(this::optimizeError)
                .toArray(BaseSequence[]::new);
    }

    private BaseSequence optimizeError(BaseSequence seq) {
        return IntStream.range(0, addressErrorOpts)
                .mapToObj(i -> seq.permute(getPermutation(i)))
                .peek(p -> p.putProperty(ERROR_PROPERTY_NAME, errorScore(seq)))
                .min(Comparator.comparing(p -> p.getProperty(ERROR_PROPERTY_NAME)))
                .orElseThrow();
    }
}
