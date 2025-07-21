package dnacoders.tree.sketchers;

import core.Attribute;
import core.BaseSequence;
import core.dnarules.DNARule;
import core.dnarules.SuperBasicDNARules;
import datastructures.reference.IDNASketch;
import dnacoders.AttributeMapper;
import dnacoders.DistanceCoder;
import dnacoders.dnaconvertors.RotatingQuattro;
import dnacoders.headercoders.BasicDNAPadder;
import utils.*;
import utils.lsh.LSH;
import utils.lsh.minhash.MinHashLSH;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class AbstractHashSketcher implements IDNASketcher<IDNASketch.HashSketch> {
    public static final String ERROR_PROPERTY_NAME = "error";
    public static final String DIST_PROPERTY_NAME = "dist";
    public static final String SCORE_PROPERTY_NAME = "score";

    protected static final Function<Integer, Coder<BaseSequence, BaseSequence>> PADDER_FUNC = BasicDNAPadder::new;

    protected final int addressSize;
    protected final LSH<BaseSequence> lsh;
    protected final boolean parallel;
    protected final boolean manageLsh;
    protected final int addressErrorOpts;
    protected final float errorWeight;
    protected final float distanceWeight;
    protected final AtomicLong seedCounter;
    protected final DNARule errorRules;
    protected final Coder<String, BaseSequence> stringCoder;
    protected final AsymmetricCoder<Attribute<?>, Attribute<String>, BaseSequence> attributeCoder;
    protected final Map<Integer, Permutation> permutationsMap;
    protected final AsymmetricCoder<IDNASketch.HashSketch, DecodedSketch<IDNASketch.HashSketch>, BaseSequence> coder;

    public AbstractHashSketcher(int addressSize, LSH<BaseSequence> lsh, DNARule errorRules, Coder<String, BaseSequence> stringCoder, boolean parallel, boolean manageLsh, int addressErrorOpts, float errorWeight, float distanceWeight) {
        this.addressSize = addressSize;
        this.lsh = lsh;
        this.parallel = parallel;
        this.manageLsh = manageLsh;
        this.addressErrorOpts = addressErrorOpts;
        this.errorWeight = errorWeight;
        this.distanceWeight = distanceWeight;
        this.seedCounter = new AtomicLong(0L);
        this.permutationsMap = new ConcurrentHashMap<>();
        this.stringCoder = stringCoder;
        this.errorRules = errorRules;
        this.attributeCoder = AttributeMapper.newInstance(stringCoder);
        this.coder = new DNAHashSketchCoder(this, parallel);
    }

    protected abstract Stream<BaseSequence> generateCandidates(int total, long seed);

    @Override
    public IDNASketch.HashSketch createSketch(int n, int tol) {
        int total = n + tol;
        long seed = seedCounter.getAndIncrement();
        List<Pair<Integer, BaseSequence>> sortedCandidates =
                FuncUtils.stream(FuncUtils.enumerate(
                                        FuncUtils.stream(generateCandidates(total, seed), parallel)
                                                .toList()
                                                .stream()
                                )
                                .toList()
                                .stream(), parallel)
                        .peek(p -> p.getT2().putProperty(SCORE_PROPERTY_NAME, score(p.getT2(), errorWeight, distanceWeight)))
                        .sorted(Comparator.comparing(p -> p.getT2().getProperty(SCORE_PROPERTY_NAME)))
                        .toList();


        int[] badIds = sortedCandidates.subList(0, tol).stream().mapToInt(Pair::getT1).toArray();
        Stream<BaseSequence> bestAddressesStream = FuncUtils.stream(sortedCandidates.subList(tol, total).stream(), parallel).sorted(Comparator.comparing(Pair::getT1)).map(Pair::getT2);
        if (manageLsh)
            bestAddressesStream = bestAddressesStream.peek(lsh::insert);

        BaseSequence[] bestAddresses = bestAddressesStream.toArray(BaseSequence[]::new);

        return new IDNASketch.HashSketch(
                seed,
                n,
                badIds,
                bestAddresses
        );
    }

    protected IntStream usedIndices(int n, int[] badIndices) {
        return FuncUtils.stream(IDNASketch.HashSketch.computeUsedIds(n, badIndices), parallel);
    }

    @Override
    public AsymmetricCoder<IDNASketch.HashSketch, DecodedSketch<IDNASketch.HashSketch>, BaseSequence> coder() {
        return coder;
    }

    @Override
    public int addressSize() {
        return addressSize;
    }

    @Override
    public LSH<BaseSequence> getLSH() {
        return lsh;
    }

    protected Permutation getPermutation(int id) {
        return permutationsMap.computeIfAbsent(id, t -> FuncUtils.getUniformPermutation(t, addressSize));
    }

    protected BaseSequence limitToAddressSize(BaseSequence candidate) {
        return candidate.length() <= addressSize ? candidate : candidate.window(0, addressSize);
    }

    public static Builder builder() {
        return new Builder();
    }

    public float errorScore(BaseSequence seq) {
        return seq.getProperty(ERROR_PROPERTY_NAME, () -> SuperBasicDNARules.INSTANCE.evalErrorProbability(seq));
    }

    public float distScore(BaseSequence seq) {
        return seq.getProperty(DIST_PROPERTY_NAME, () -> Math.min(DistanceCoder.distanceScoreExclusive(seq, getLSH()), DistanceCoder.distanceScore(seq.complement(), getLSH())));
    }

    public float score(BaseSequence seq, float errorWeight, float distanceWeight) {
        final float errorScore;
        final float distScore;

        if (errorWeight != 0.0f)
            errorScore = errorScore(seq);
        else
            errorScore = 0.0f;

        if (distanceWeight != 0.0f)
            distScore = distScore(seq);
        else
            distScore = 0.0f;

        return seq.getProperty(SCORE_PROPERTY_NAME, () -> -errorWeight * errorScore + distanceWeight * distScore);
    }


    public static class Builder {

        public enum Flavor {
            F1, F2
        }

        public static final int DEFAULT_ADDRESS_SIZE = 80;
        public static final int DEFAULT_ADDRESS_ERROR_OPTS = 3;

        public static final DNARule DEFAULT_ERROR_RULES = SuperBasicDNARules.INSTANCE;

        public static final float DEFAULT_ERROR_WEIGHT = 1.0f;
        public static final float DEFAULT_DIST_WEIGHT = 1.0f;

        public static final Flavor DEFAULT_FLAVOR = Flavor.F1;
        public static final boolean DEFAULT_PARALLEL = true;
        public static final boolean DEFAULT_MANAGE_LSH = true;
        public static final Function<Integer, LSH<BaseSequence>> DEFAULT_LSH = addrSize -> MinHashLSH.newSeqLSHTraditional(5 + Math.max(1, addrSize / 80), 5);
        public static final Coder<String, BaseSequence> DEFAULT_STRING_CODER = RotatingQuattro.INSTANCE;

        private Integer addressSize;
        private Integer addressErrorOpts;
        private Flavor flavor;
        private Boolean parallel;
        private Boolean manageLsh;
        private LSH<BaseSequence> lsh;
        private Float errorWeight;
        private Float distanceWeight;
        private DNARule errorRules;
        protected Coder<String, BaseSequence> stringCoder;

        public IDNASketcher<IDNASketch.HashSketch> build() {
            this.addressSize = FuncUtils.conditionOrElse(s -> s != null && s > 0, addressSize, () -> DEFAULT_ADDRESS_SIZE);
            this.addressErrorOpts = FuncUtils.conditionOrElse(s -> s != null && s >= 0, addressErrorOpts, () -> DEFAULT_ADDRESS_ERROR_OPTS);
            this.flavor = FuncUtils.nullEscape(flavor, () -> DEFAULT_FLAVOR);
            this.parallel = FuncUtils.nullEscape(parallel, () -> DEFAULT_PARALLEL);
            this.manageLsh = FuncUtils.nullEscape(manageLsh, () -> DEFAULT_MANAGE_LSH);
            this.lsh = FuncUtils.nullEscape(lsh, () -> DEFAULT_LSH.apply(addressSize));

            this.stringCoder = FuncUtils.nullEscape(stringCoder, () -> DEFAULT_STRING_CODER);
            this.errorRules = FuncUtils.nullEscape(errorRules, DEFAULT_ERROR_RULES);

            this.errorWeight = FuncUtils.nullEscape(errorWeight, () -> DEFAULT_ERROR_WEIGHT);
            this.distanceWeight = FuncUtils.nullEscape(distanceWeight, () -> DEFAULT_DIST_WEIGHT);

            return switch (flavor) {
                case F1 -> new DNASketcherV1(addressSize, lsh, errorRules, stringCoder, parallel, manageLsh, addressErrorOpts, errorWeight, distanceWeight);
                case F2 -> new DNASketcherV2(addressSize, lsh, errorRules, stringCoder, parallel, manageLsh, addressErrorOpts, errorWeight, distanceWeight);
            };
        }

        public Builder setAddressSize(int addressSize) {
            this.addressSize = addressSize;
            return this;
        }

        public Builder setErrorWeight(float errorWeight) {
            this.errorWeight = errorWeight;
            return this;
        }

        public Builder setDistanceWeight(float distanceWeight) {
            this.distanceWeight = distanceWeight;
            return this;
        }

        public Builder setFlavor(Flavor flavor) {
            this.flavor = flavor;
            return this;
        }

        public Builder setParallel(boolean parallel) {
            this.parallel = parallel;
            return this;
        }

        public Builder setManageLsh(boolean manageLsh) {
            this.manageLsh = manageLsh;
            return this;
        }

        public Builder setLsh(LSH<BaseSequence> lsh) {
            this.lsh = lsh;
            return this;
        }

        public Builder setAddressErrorOpts(int addressErrorOpts) {
            this.addressErrorOpts = addressErrorOpts;
            return this;
        }

        public Builder setStringCoder(Coder<String, BaseSequence> stringCoder) {
            this.stringCoder = stringCoder;
            return this;
        }

        public Builder setErrorRules(DNARule errorRules) {
            this.errorRules = errorRules;
            return this;
        }
    }
}
