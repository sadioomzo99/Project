package generators;

import core.Attribute;
import core.BaseSequence;
import core.dnarules.BasicDNARules;
import dnacoders.AttributeMapper;
import dnacoders.dnaconvertors.RotatingTre;
import dnacoders.headercoders.BasicDNAPadder;
import utils.AsymmetricCoder;
import utils.Coder;
import utils.FuncUtils;
import utils.Permutation;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.IntStream;

public class SeqGeneratorAttributed implements SeqGenerator {

    private static final int MAX_TRIALS = 10;
    private static final float DEFAULT_MAX_ERROR = 0.6f;
    private static final String DEFAULT_ATTRIBUTE_NAME = "id";
    private static final Function<BaseSequence, Boolean> DEFAULT_RULE = seq -> BasicDNARules.INSTANCE.evalErrorProbability(seq) <= DEFAULT_MAX_ERROR;

    private final Permutation[] permutations;
    private final BasicDNAPadder padder;
    private final AsymmetricCoder<Attribute<?>, Attribute<String>, BaseSequence> attributeMapper;
    private final int length;
    private final AtomicLong counter;
    private final Function<BaseSequence, Boolean> rule;


    public SeqGeneratorAttributed(int length, Function<BaseSequence, Boolean> rule, Coder<String, BaseSequence> stringCoder) {
        this.padder = new BasicDNAPadder(length);
        this.attributeMapper = AttributeMapper.newInstance(stringCoder);
        this.length = length;
        this.counter = new AtomicLong(0L);
        this.rule = rule;
        this.permutations = IntStream.range(0, MAX_TRIALS).mapToObj(id -> FuncUtils.getUniformPermutation(id, length)).toArray(Permutation[]::new);
    }

    public SeqGeneratorAttributed(int length) {
        this(length, DEFAULT_RULE, RotatingTre.INSTANCE);
    }

    @Override
    public BaseSequence generate() {
        BaseSequence seq = padder.encode(attributeMapper.encode(new Attribute<>(DEFAULT_ATTRIBUTE_NAME, String.valueOf(counter.getAndIncrement()))));
        BaseSequence r = seq.length() > length ? seq.window(0, length) : seq;
        int trials = MAX_TRIALS;
        while (trials-- > 0) {
            if (!rule.apply(r))
                r.permuteInPlace(permutations[trials]);
            else
                return r;
        }

        return r;
    }
}
