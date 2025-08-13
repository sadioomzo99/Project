package utils.sequencing;

import utils.Range;
import java.math.BigInteger;
import java.time.Duration;
import java.util.*;

public class SequencingPlatform {

    public static final SequencingPlatform SANGER = new SequencingPlatform(
            "Sanger",
            Generation.FIRST,
            List.of(
                    new RunOption(
                            new Range.NumberRange<>(50, 1000),
                            96L,
                            1,
                            Duration.ofHours(1L),
                            (count, seqLength, __) -> 0.5d * count * seqLength
                    )
            )
    );


    public static final SequencingPlatform ILLUMINA_ISEQ_100 = new SequencingPlatform(
            "Illumina (iSeq 100)",
            Generation.SECOND,
            List.of(
                  new RunOption(
                          36,
                          4_000_000L,
                          1,
                          Duration.ofHours(9L).plusMinutes(30L),
                          (__, seqLength, coverage) -> 0.0000005d * 4_000_000 * seqLength * coverage
                  ),
                    new RunOption(
                            50,
                            4_000_000L,
                            1,
                            Duration.ofHours(10L),
                            (__, seqLength, coverage) -> 0.0000005d * 4_000_000 * seqLength * coverage
                    ),
                    new RunOption(
                            75,
                            4_000_000L,
                            1,
                            Duration.ofHours(11L),
                            (__, seqLength, coverage) -> 0.0000005d * 4_000_000 * seqLength * coverage
                    ),
                    new RunOption(
                            75,
                            4_000_000L,
                            2,
                            Duration.ofHours(14L),
                            (__, seqLength, coverage) -> 0.0000005d * 4_000_000 * seqLength * coverage
                    ),
                    new RunOption(
                            150,
                            4_000_000L,
                            2,
                            Duration.ofHours(19L),
                            (__, seqLength, coverage) -> 0.0000005d * 4_000_000 * seqLength * coverage
                    )
            )
    );


    public static final SequencingPlatform ILLUMINA_MINI_SEQ = new SequencingPlatform(
            "Illumina (MiniSeq)",
            Generation.SECOND,
            List.of(
                    // https://www.mdc-berlin.de/system/files/document/Genomics_Platforms_Pricelist_20210415_1.pdf
                    new RunOption(
                            75,
                            25_000_000L,
                            1,
                            Duration.ofHours(7L),
                            846.18d
                    ),
                    new RunOption(
                            150,
                            25_000_000L,
                            1,
                            Duration.ofHours(13L),
                            992.15d
                    ),
                    new RunOption(
                            75,
                            25_000_000L,
                            2,
                            Duration.ofHours(7L),
                            992.15d
                    ),
                    new RunOption(
                            75,
                            25_000_000L,
                            2,
                            Duration.ofHours(7L),
                            992.15d
                    ),
                    new RunOption(
                            150,
                            25_000_000L,
                            2,
                            Duration.ofHours(13L),
                            1587.12d
                    ),
                    new RunOption(
                            150,
                            8_000_000L,
                            2,
                            Duration.ofHours(7L),
                            565.56d
                    ),
                    // spec sheet
                    new RunOption(
                            300,
                            25_000_000L,
                            1,
                            Duration.ofHours(24L),
                            (__, seqLength, coverage) -> 0.00000042d * 25_000_000 * seqLength * coverage
                    ),
                    new RunOption(
                            300,
                            25_000_000L,
                            2,
                            Duration.ofHours(24L),
                            (__, seqLength, coverage) -> 0.00000042d * 25_000_000 * seqLength * coverage
                    ),
                    new RunOption(
                            100,
                            20_000_000L,
                            1,
                            Duration.ofHours(5L),
                            (__, seqLength, coverage) -> 0.0000005d * 20_000_000 * seqLength * coverage
                    ),
                    new RunOption(
                            300,
                            8_000_000L,
                            1,
                            Duration.ofHours(17L),
                            (__, seqLength, coverage) -> 0.00000042d * 8_000_000 * seqLength * coverage
                    ),
                    new RunOption(
                            300,
                            8_000_000L,
                            2,
                            Duration.ofHours(17L),
                            (__, seqLength, coverage) -> 0.00000042d * 8_000_000 * seqLength * coverage
                    )
            )
    );


    public static final SequencingPlatform ILLUMINA_NEXT_SEQ_550 = new SequencingPlatform(
            "Illumina (NextSeq550)",
            Generation.SECOND,
            List.of(
                    new RunOption(
                            75,
                            400_000_000L,
                            1,
                            Duration.ofHours(11L),
                            1520.83d
                    ),
                    new RunOption(
                            75,
                            400_000_000L,
                            2,
                            Duration.ofHours(18L),
                            2913.68d
                    ),
                    new RunOption(
                            150,
                            400_000_000L,
                            2,
                            Duration.ofHours(18L),
                            4666.40d
                    ),
                    new RunOption(
                            150,
                            130_000_000L,
                            1,
                            Duration.ofHours(15L),
                            1108.30d
                    ),
                    new RunOption(
                            75,
                            130_000_000L,
                            2,
                            Duration.ofHours(15L),
                            1108.30d
                    ),
                    new RunOption(
                            150,
                            130_000_000L,
                            2,
                            Duration.ofHours(19L),
                            1786.14d
                    )
            )
    );

    public static final SequencingPlatform ILLUMINA_NEXT_SEQ_1000 = new SequencingPlatform(
            "Illumina (NextSeq 1000)",
            Generation.SECOND,
            List.of(
                    new RunOption(
                            150,
                            100_000_000L,
                            2,
                            Duration.ofHours(19L),
                            2005.00d
                    ),
                    new RunOption(
                            300,
                            100_000_000L,
                            1,
                            Duration.ofHours(19L),
                            2005.00d
                    ),
                    new RunOption(
                            300,
                            100_000_000L,
                            2,
                            Duration.ofHours(34L),
                            2720.00d
                    )
            )
    );

    public static final SequencingPlatform ILLUMINA_NEXT_SEQ_2000 = new SequencingPlatform(
            "Illumina (NextSeq 2000)",
            Generation.SECOND,
            List.of(
                    new RunOption(
                            150,
                            1_200_000_000L,
                            2,
                            Duration.ofHours(48L),
                            6555.00d
                    ),
                    new RunOption(
                            300,
                            1_200_000_000L,
                            1,
                            Duration.ofHours(48L),
                            6555.00d
                    )
            )
    );

    public static final SequencingPlatform ILLUMINA_NOVA_SEQ_6000 = new SequencingPlatform(
            "Illumina (NovaSeq 6000)",
            Generation.SECOND,
            List.of(
                    new RunOption(
                            250,
                            40_000_000_000L,
                            2,
                            Duration.ofHours(38L),
                            101533.6d
                    ),
                    new RunOption(
                            250,
                            80_000_000_000L,
                            1,
                            Duration.ofHours(38L),
                            101533.6d
                    ),
                    new RunOption(
                            150,
                            20_000_000_000L,
                            2,
                            Duration.ofHours(44L),
                            6000.6d
                    ),
                    new RunOption(
                            150,
                            20_000_000_000L,
                            1,
                            Duration.ofHours(44L),
                            6000.0d
                    )
            )
    );

    public static final SequencingPlatform ONT_FONGLE = new SequencingPlatform(
            "Oxford Nanopore Sequencing Technology (Fongle)",
            Generation.THIRD,
            List.of(
                    new RunOption(
                            new Range.NumberRange<>(20, 2_000_000),
                            1400L,
                            1,
                            (count, seqLen, coverage) -> Duration.ofSeconds(count * seqLen * coverage / 420L),
                            (count, seqLen, coverage) -> 37.50d / (1000_000_000d) * count * seqLen * coverage
                    ) {
                        @Override
                        public boolean isFeasible(long count, int seqLength, int coverage) {
                            return fitsLength(seqLength) && count * seqLength < 2_800_000_000L;
                        }
                        public long getNumSequences(int seqLength) {
                            return 2_800_000_000L / seqLength;
                        }
                    }
            )
    );

    public static final SequencingPlatform ONT_MINION = new SequencingPlatform(
            "Oxford Nanopore Sequencing Technology (MinION)",
            Generation.THIRD,
            List.of(
                    new RunOption(
                            new Range.NumberRange<>(20, 2_000_000),
                            25_000L,
                            1,
                            (count, seqLen, coverage) -> Duration.ofSeconds(count * seqLen * coverage / 192901L),
                            (count, seqLen, coverage) -> 13.13d / 1000_000_000d * count * seqLen * coverage
                    ) {
                        @Override
                        public boolean isFeasible(long count, int seqLength, int coverage) {
                            return fitsLength(seqLength) && count * seqLength < 50_000_000_000L;
                        }
                        public long getNumSequences(int seqLength) {
                            return 50_000_000_000L / seqLength;
                        }
                    }
            )
    );

    public static final SequencingPlatform ONT_GRID_ION = new SequencingPlatform(
            "Oxford Nanopore Sequencing Technology (GridION)",
            Generation.THIRD,
            List.of(
                    new RunOption(
                            new Range.NumberRange<>(20, 2_000_000),
                            125_000L,
                            1,
                            (count, seqLen, coverage) -> Duration.ofSeconds(count * seqLen * coverage / 964506L),
                            (count, seqLen, coverage) -> 13.13d / 1000_000_000d * count * seqLen * coverage
                    ) {
                        @Override
                        public boolean isFeasible(long count, int seqLength, int coverage) {
                            return fitsLength(seqLength) && count * seqLength < 250_000_000_000L;
                        }
                        public long getNumSequences(int seqLength) {
                            return 250_000_000_000L / seqLength;
                        }
                    }
            )
    );

    protected final String name;
    protected final Generation generation;
    protected final Map<String, String> properties;
    protected final List<RunOption> runOptions;

    public SequencingPlatform(String name, Generation generation, List<RunOption> runOptions) {
        this(
                name,
                generation,
                new HashMap<>(),
                runOptions
        );
    }

    public SequencingPlatform(String name, Generation generation, Map<String, String> properties, List<RunOption> runOptions) {
        this.name = name;
        this.generation = generation;
        this.properties = properties;
        this.runOptions = runOptions;
        this.runOptions.forEach(ro -> ro.setPlatform(this));
    }

    public boolean isFeasible(long count, int sequenceLength, int coverage) {
        List<RunOption> potentialOptions = runOptions.stream().filter(run -> run.isFeasible(count, sequenceLength, coverage)).toList();
        return !potentialOptions.isEmpty();
    }

    public SequencingResult sequence(long count, int sequenceLength, int coverage) {
        List<RunOption> potentialOptions = runOptions.stream().filter(run -> run.getSeqLengthRange().intersects(sequenceLength)).toList();
        if (potentialOptions.isEmpty())
            throw new RuntimeException("no run option found to sequence your request");

        long targetCycles = sequenceLength * count * coverage;
        List<RunOption> options = potentialOptions.stream().sorted(Comparator.comparingLong(r -> r.totalCycles(sequenceLength) - targetCycles)).toList();

        if (options.isEmpty())
            options = potentialOptions;

        List<SequencingRunResult> results = new ArrayList<>();
        long countRemaining = count;
        for (RunOption option : options) {
            if (countRemaining == 0L)
                break;

            long countOnOption = Math.min(countRemaining, option.getNumSequences(sequenceLength));
            int optionsCoverage = option.getCoverage();
            countRemaining -= countOnOption;
            if (optionsCoverage >= coverage)
                results.add(option.run(countOnOption, sequenceLength));
            else {
                int currentCoverage = 0;
                do {
                    results.add(option.run(countOnOption, sequenceLength));
                    currentCoverage++;

                } while(currentCoverage < coverage);
            }
        }

        return new SequencingResult(results);
    }

    public SequencingResult sequence(double _count, int sequenceLength, int coverage) {
        var s = Double.toString(Math.ceil(_count));
        BigInteger count = new BigInteger(s.substring(0, s.length() - 2));
        List<RunOption> potentialOptions = runOptions.stream().filter(run -> run.getSeqLengthRange().intersects(sequenceLength)).toList();
        if (potentialOptions.isEmpty())
            throw new RuntimeException("no run option found to sequence your request");

        BigInteger targetCycles = count.multiply(BigInteger.valueOf(sequenceLength)).multiply(BigInteger.valueOf(coverage));
        List<RunOption> options = potentialOptions.stream().sorted(Comparator.comparingDouble(r -> r.totalCycles(sequenceLength) - targetCycles.doubleValue())).toList();

        if (options.isEmpty())
            options = potentialOptions;

        List<SequencingRunResult> results = new ArrayList<>();
        BigInteger countRemaining = count;
        for (RunOption option : options) {
            if (countRemaining.compareTo(BigInteger.ZERO) == 0)
                break;

            s = Double.toString(
                    Math.min(
                            countRemaining.doubleValue(),
                            option.getNumSequences(sequenceLength)
                    )
            );
            BigInteger countOnOption = new BigInteger(
                    s.substring(0, s.length() - 2)
            );
            int optionsCoverage = option.getCoverage();
            countRemaining = countRemaining.subtract(countOnOption);
            if (optionsCoverage >= coverage)
                results.add(option.run(countOnOption.longValue(), sequenceLength));
            else {
                int currentCoverage = 0;
                do {
                    results.add(option.run(countOnOption.longValue(), sequenceLength));
                    currentCoverage++;

                } while(currentCoverage < coverage);
            }
        }

        return new SequencingResult(results);
    }

    public String getName() {
        return name;
    }

    public Generation getGeneration() {
        return generation;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public List<RunOption> getRunOptions() {
        return runOptions;
    }
}
