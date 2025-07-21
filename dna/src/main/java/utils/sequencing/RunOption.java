package utils.sequencing;

import utils.FuncUtils;
import utils.Range;
import java.time.Duration;

public class RunOption {
    protected SequencingPlatform platform;
    protected final Range.NumberRange<Integer> seqLengthRange;
    protected final long numSequences;
    protected final int coverage;
    protected final FuncUtils.TriFunction<Long, Integer, Integer, Duration> runTimeFunc;
    protected final FuncUtils.TriFunction<Long, Integer, Integer, Double> costFunc;


    public RunOption(int seqLength, long numSequences, int coverage, Duration runTime, double cost) {
        this(
                new Range.NumberRange<>(seqLength, seqLength),
                numSequences,
                coverage,
                (__, ___, ____) -> runTime,
                (__, ___, ____) -> cost
        );
    }

    public RunOption(int seqLength, long numSequences, int coverage, Duration runTime, FuncUtils.TriFunction<Long, Integer, Integer, Double> costFunc) {
        this(
                new Range.NumberRange<>(seqLength, seqLength),
                numSequences,
                coverage,
                (__, ___, ____) -> runTime,
                costFunc
        );
    }

    public RunOption(Range.NumberRange<Integer> seqLengthRange, long numSequences, int coverage, Duration runTime, double cost) {
        this(
                seqLengthRange,
                numSequences,
                coverage,
                (__, ___, ____) -> runTime,
                (__, ___, ____) -> cost
        );
    }

    public RunOption(Range.NumberRange<Integer> seqLengthRange, long numSequences, int coverage, FuncUtils.TriFunction<Long, Integer, Integer, Duration> runTimeFunc, double cost) {
        this(
                seqLengthRange,
                numSequences,
                coverage,
                runTimeFunc,
                (__, ___, ____) -> cost
        );
    }

    public RunOption(Range.NumberRange<Integer> seqLengthRange, long numSequences, int coverage, Duration runTime, FuncUtils.TriFunction<Long, Integer, Integer, Double> costFunc) {
        this(
                seqLengthRange,
                numSequences,
                coverage,
                (__, ___, ____) -> runTime,
                costFunc
        );
    }


    public RunOption(int seqLength, long numSequences, int coverage, FuncUtils.TriFunction<Long, Integer, Integer, Duration> runTimeFunc, FuncUtils.TriFunction<Long, Integer, Integer, Double> costFunc) {
        this(
                new Range.NumberRange<>(seqLength, seqLength),
                numSequences,
                coverage,
                runTimeFunc,
                costFunc
        );
    }

    public RunOption(Range.NumberRange<Integer> seqLengthRange, long numSequences, int coverage, FuncUtils.TriFunction<Long, Integer, Integer, Duration> runTimeFunc, FuncUtils.TriFunction<Long, Integer, Integer, Double> costFunc) {
        this.seqLengthRange = seqLengthRange;
        this.numSequences = numSequences;
        this.coverage = coverage;
        this.runTimeFunc = runTimeFunc;
        this.costFunc = costFunc;
    }

    protected RunOption setPlatform(SequencingPlatform platform) {
        this.platform = platform;
        return this;
    }

    public long maxTotalCycles() {
        return totalCycles(seqLengthRange.max());
    }

    public long totalCycles(int sequenceLength) {
        return numSequences * sequenceLength;
    }

    public boolean fitsLength(int seqLength) {
        return this.seqLengthRange.intersects(seqLength);
    }

    public boolean isFeasible(long count, int seqLength, int coverage) {
        return fitsLength(seqLength);
    }

    /**
     * Returns the sequencing speed for a uniquely read nucleotide without coverage. That is, if the coverage is 2, this speed is half of totalNucleotidesPerSec().
     * @return the sequencing speed in nucleotides / second.
     */
    public double nucleotidesPerSec() {
        return totalNucleotidesPerSec() / coverage;
    }

    /**
     * Returns the true sequencing speed for every read nucleotide. Note that potentially more nucleotides are read with higher coverage.
     * @return the sequencing speed in nucleotides / second.
     */
    public double totalNucleotidesPerSec() {
        return (double) maxTotalCycles() / runTimeFunc.apply(numSequences, seqLengthRange.max(), coverage).toSeconds();
    }


    public SequencingRunResult run(long count, int seqLength, int coverage) {
        if (!isFeasible(count, seqLength, coverage))
            throw new RuntimeException("the applied sequencing configuration is not compatible with this run: " + count + ", " + seqLength + ", " + coverage + ", " + this.platform.getName());

        long totalNucleotides = count * seqLength * coverage;
        return new SequencingRunResult(
                platform,
                this,
                runTimeFunc.apply(count, seqLength, coverage),
                costFunc.apply(count, seqLength, coverage),
                count,
                maxTotalCycles() - totalNucleotides,
                seqLength,
                coverage
        );
    }

    public SequencingRunResult run(long count, int seqLength) {
        return run(count, seqLength, coverage);
    }

    public SequencingRunResult run(long count) {
        return run(count, seqLengthRange.max());
    }

    public Range.NumberRange<Integer> getSeqLengthRange() {
        return seqLengthRange;
    }

    public long getNumSequences() {
        return numSequences;
    }

    public long getNumSequences(int seqLength) {
        return getNumSequences();
    }

    public int getCoverage() {
        return coverage;
    }

    public FuncUtils.TriFunction<Long, Integer, Integer, Duration> getRunTimeFunc() {
        return runTimeFunc;
    }

    public FuncUtils.TriFunction<Long, Integer, Integer, Double> getCostFunc() {
        return costFunc;
    }

    @Override
    public String toString() {
        return "RunOption{" +
                "platform=" + platform.getName() +
                ", seqLengthRange=" + seqLengthRange +
                ", numSequences=" + numSequences +
                ", coverage=" + coverage +
                "}";
    }
}
