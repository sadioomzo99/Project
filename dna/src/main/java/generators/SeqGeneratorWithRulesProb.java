package generators;

import core.BaseSequence;
import core.dnarules.DNARulesCollection;
import utils.FuncUtils;
import utils.Permutation;
import utils.Range;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class SeqGeneratorWithRulesProb implements SeqGenerator {
    public static final int DEFAULT_LEN = 60;
    private static final int MAX_TRIALS = 10;
    public static final Range<Double> DEFAULT_GC_RANGE = new Range<>(0.4d, 0.6d);

    private final DNARulesCollection rules;
    private final Permutation[] permutations;
    private final int len;
    private final double gcMin;
    private final double gcMax;
    private final float maxError;

    public SeqGeneratorWithRulesProb(float maxError, DNARulesCollection rules) {
        this(DEFAULT_LEN, DEFAULT_GC_RANGE, maxError, rules);
    }

    public SeqGeneratorWithRulesProb(int len, Range<Double> gcRange, float maxError, DNARulesCollection rules) {
        this.len = len;
        this.gcMin = gcRange.getT1();
        this.gcMax = gcRange.getT2();
        this.maxError = maxError;
        this.rules = rules;
        this.permutations = IntStream.range(0, MAX_TRIALS).mapToObj(id -> FuncUtils.getUniformPermutation(id, len)).toArray(Permutation[]::new);
    }

    @Override
    public BaseSequence generate() {
        BaseSequence seq = BaseSequence.random(len, ThreadLocalRandom.current().nextDouble(gcMin, gcMax));
        int trials = MAX_TRIALS;
        while (trials-- > 0) {
            if (rules.evalErrorProbability(seq) > maxError)
                seq.permuteInPlace(permutations[trials]);
            else
                return seq;
        }

        return seq;

        //return Stream.generate(() -> BaseSequence.random(len, FuncUtils.randomSample(gcRange))).filter(seq -> rules.evalErrorByLimit(seq, maxError) <= maxError).findFirst().orElseThrow();
    }

    /*
    @Override
    public Stream<BaseSequence> stream() {
        return Stream.generate(() -> BaseSequence.random(len, FuncUtils.randomSample(gcRange))).filter(seq -> rules.evalErrorByLimit(seq, maxError) <= maxError);
    }

     */
}
