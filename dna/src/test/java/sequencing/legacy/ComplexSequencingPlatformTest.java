package sequencing.legacy;

import sequencing.utils.Commons;
import utils.FuncUtils;
import utils.sequencing.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import utils.csv.BufferedCsvWriter;

public class ComplexSequencingPlatformTest {

    public static void main(String[] args) {
        List<SequencingPlatform> NGS = Arrays.asList(
                SequencingPlatform.ILLUMINA_ISEQ_100,
                SequencingPlatform.ILLUMINA_MINI_SEQ,
                SequencingPlatform.ILLUMINA_NEXT_SEQ_550,
                SequencingPlatform.ILLUMINA_NEXT_SEQ_1000,
                SequencingPlatform.ILLUMINA_NEXT_SEQ_2000,
                SequencingPlatform.ILLUMINA_NOVA_SEQ_6000
        );

        List<SequencingPlatform> SANGER = List.of(
                SequencingPlatform.SANGER
        );

        List<SequencingPlatform> ONT = List.of(
                SequencingPlatform.ONT_FONGLE,
                SequencingPlatform.ONT_MINION,
                SequencingPlatform.ONT_GRID_ION
        );

        List<List<SequencingPlatform>> platforms = List.of(SANGER, NGS, ONT);

        Map<List<SequencingPlatform>, Commons.StrandConfig> configs = new HashMap<>();
        configs.put(SANGER, new Commons.StrandConfig(80, 1000 - 80));
        configs.put(NGS, new Commons.StrandConfig(80, 250 - 80));
        configs.put(ONT, new Commons.StrandConfig(80, 10_000 - 80));


        BufferedCsvWriter writer = new BufferedCsvWriter("D:/Promotion/paper_4/results/new/sequencing_analysis_complex.csv", false);
        writer.appendNewLine(
                "platformInternalNodes_name",
                "platformInternalNodes_generation",
                "platformInternalNodes_readLength_min",
                "platformInternalNodes_readLength_max",
                "platformInternalNodes_costPerGigaBase_min",
                "platformInternalNodes_costPerGigaBase_max",
                "platformInternalNodes_costPerGigaBase_avg",

                "platformLeafNodes_name",
                "platformLeafNodes_generation",
                "platformLeafNodes_readLength_min",
                "platformLeafNodes_readLength_max",
                "platformLeafNodes_costPerGigaBase_min",
                "platformLeafNodes_costPerGigaBase_max",
                "platformLeafNodes_costPerGigaBase_avg",

                "pointerSize",
                "bitrate",
                "b",
                "c",
                "treeHeight",
                "numInternalNodes",
                "numLeafNodes",
                "numPointers",
                "numberTuples",
                "tupleSize",
                "selectivity",
                "bufferedInternalNodes",
                "bufferedLeafNodes",

                "pointersAsGigaNucleotidesRaw",
                "objectsAsGigaNucleotidesRaw",
                "pointersGigaNucleotides", // with respect to bitrate
                "objectsGigaNucleotides", // with respect to bitrate

                "numGigaNucleotidesPerInternalNode",
                "numGigaNucleotidesPerLeafNode",

                "querySequencingCost",
                "total_sequencing_platform_internalNodes",
                "total_sequencing_platform_internalNodes_generation",
                "total_sequencing_platform_leafNodes",
                "total_sequencing_platform_leafNodes_generation",
                "totalCostReadAll",
                "savedCostByTree"

        );


        Map<Generation, Long> sequenceAllInternalNodesFreqs = new HashMap<>();
        Map<Generation, Long> sequenceAllLeafNodesFreqs = new HashMap<>();

        Map<Generation, Long> sequenceQueryInternalNodesFreqs = new HashMap<>();
        Map<Generation, Long> sequenceQueryLeafNodesFreqs = new HashMap<>();

        long pointerSize = 100;
        double pointersBitrate = 2d;
        for (float bitrate : List.of(2f, 1.6f, 1.2f, 0.8f, 0.4f)) {
            for (int b : List.of(2, 16, 64, 128, 256, 512)) {
                int internalNodePointers = 2 * b - 1;
                for (int c : List.of(2, 16, 64, 128, 256, 512)) {
                    int leafSize = 2 * c - 1;
                    for (long numberTuples : List.of(1000L, 100_000L, 1_000_000L, 100_000_000L, 10_000_000_000L)) {
                        for (long tupleSize : List.of(100L, 1000L, 10_000L)) {
                            for (double selectivity : List.of(0.001d, 0.01d, 0.1d, 0.2d, 0.5d)) {
                                long numLeafNodes = (int) Math.ceil((double) numberTuples / leafSize);
                                long numInternalNodes = numLeafNodes / (b - 1);

                                for (int bufferedInternalNodes : DoubleStream.of(0d, 0.001d, 0.01d, 0.1d, 0.2d, 0.5d).mapToInt(factor -> (int) (Math.ceil(factor * numInternalNodes))).toArray()) {
                                    for (int bufferedLeafNodes : DoubleStream.of(0d, 0.001d, 0.01d, 0.1d, 0.2d, 0.5d).mapToInt(factor -> (int) (Math.ceil(factor * numLeafNodes))).toArray()) {
                                        Commons.QueryPlan queryEvaluationCost = Commons.visitRange(
                                                numberTuples,
                                                selectivity,
                                                b,
                                                c,
                                                bufferedInternalNodes,
                                                bufferedLeafNodes
                                        );

                                        long numPointers = numInternalNodes > 0 ?
                                                numInternalNodes * internalNodePointers // the number of children pointers
                                                        + numLeafNodes / (2L * b - 1L) : 0L; // the number of pointers to the nodes to the right (above leaves' level)

                                        double objectsAsGigaNucleotidesRaw = (double) numberTuples * tupleSize * 4d / 1000_000_000d;
                                        double pointersAsGigaNucleotidesRaw = (double) numPointers * pointerSize * 4d / 1000_000_000d;

                                        int treeHeight = (int) Math.ceil(Math.log(numLeafNodes) / Math.log(b));
                                        List<SequencingCombo> combis = new ArrayList<>(platforms.size() * platforms.size());
                                        for (List<SequencingPlatform> platformInternalNodes : platforms) {
                                            double pointersGigaNucleotides = Commons.totalGigaNts(pointersAsGigaNucleotidesRaw, configs.get(platformInternalNodes), pointersBitrate, 99.99d, Commons.accuracy(platformInternalNodes.get(0).getGeneration()));
                                            for (List<SequencingPlatform> platformLeafNodes : platforms) {
                                                double objectsGigaNucleotides = Commons.totalGigaNts(objectsAsGigaNucleotidesRaw, configs.get(platformInternalNodes), bitrate, 99.99d, Commons.accuracy(platformLeafNodes.get(0).getGeneration()));
                                                SequencingResult sequencingCostInternalNodes = platformInternalNodes.stream().map(p -> FuncUtils.superSafeCall(() -> p.sequence(pointersGigaNucleotides, configs.get(platformInternalNodes).strandSize(), 1))).filter(Objects::nonNull).min(Comparator.comparingDouble(SequencingResult::cost)).orElse(null);
                                                SequencingResult sequencingCostLeafNodes = platformLeafNodes.stream().map(p -> FuncUtils.superSafeCall(() -> p.sequence(objectsGigaNucleotides, configs.get(platformLeafNodes).strandSize(), 1))).filter(Objects::nonNull).min(Comparator.comparingDouble(SequencingResult::cost)).orElse(null);

                                                if (sequencingCostInternalNodes == null || sequencingCostLeafNodes == null)
                                                    continue;

                                                if (sequencingCostInternalNodes.sequencingRunResults() == null || sequencingCostLeafNodes.sequencingRunResults() == null)
                                                    continue;

                                                if (sequencingCostInternalNodes.sequencingRunResults().isEmpty() || sequencingCostLeafNodes.sequencingRunResults().isEmpty())
                                                    continue;

                                                double numGigaNucleotidesPerInternalNode = numInternalNodes > 0 ? pointersGigaNucleotides / numInternalNodes : 0d;
                                                double sequencingCostPerInternalNode = numInternalNodes > 0 ? sequencingCostInternalNodes.cost() / numInternalNodes : 0d;

                                                double numGigaNucleotidesPerLeafNode =  objectsGigaNucleotides / numLeafNodes;
                                                double sequencingCostPerLeafNode = sequencingCostLeafNodes.cost() / numLeafNodes;

                                                double totalCostReadAll = sequencingCostInternalNodes.cost() + sequencingCostLeafNodes.cost();


                                                double querySequencingCost = queryEvaluationCost.visitedInternalNodes() * sequencingCostPerInternalNode + queryEvaluationCost.visitedLeafNodes() * sequencingCostPerLeafNode;
                                                combis.add(
                                                        new SequencingCombo(
                                                                sequencingCostInternalNodes,
                                                                sequencingCostLeafNodes,
                                                                pointersGigaNucleotides,
                                                                objectsGigaNucleotides,
                                                                numGigaNucleotidesPerInternalNode,
                                                                sequencingCostPerInternalNode,
                                                                numGigaNucleotidesPerLeafNode,
                                                                sequencingCostPerLeafNode,
                                                                totalCostReadAll,
                                                                querySequencingCost
                                                        )
                                                );
                                            }
                                        }


                                        if (combis.isEmpty())
                                            continue;
                                        SequencingCombo bestForQuery = combis.stream().min(Comparator.comparingDouble(combo -> combo.querySequencingCost)).orElseThrow();
                                        SequencingCombo bestForSequenceAll = combis.stream().min(Comparator.comparingDouble(combo -> combo.totalCostReadAll)).orElseThrow();

                                        if (bufferedInternalNodes == 0 && bufferedLeafNodes == 0) {
                                            sequenceAllInternalNodesFreqs.merge(bestForSequenceAll.sequencingCostInternalNodes.sequencingRunResults().get(0).platform().getGeneration(), 1L, Long::sum);
                                            sequenceAllLeafNodesFreqs.merge(bestForSequenceAll.sequencingCostLeafNodes.sequencingRunResults().get(0).platform().getGeneration(), 1L, Long::sum);

                                            sequenceQueryInternalNodesFreqs.merge(bestForQuery.sequencingCostInternalNodes.sequencingRunResults().get(0).platform().getGeneration(), 1L, Long::sum);
                                            sequenceQueryLeafNodesFreqs.merge(bestForQuery.sequencingCostLeafNodes.sequencingRunResults().get(0).platform().getGeneration(), 1L, Long::sum);
                                        }

                                        writer.appendNewLine(
                                                Stream.of(
                                                        bestForQuery.sequencingCostInternalNodes.sequencingRunResults().stream().map(SequencingRunResult::platform).map(SequencingPlatform::getName).collect(Collectors.joining("|")),
                                                        bestForQuery.sequencingCostInternalNodes.sequencingRunResults().stream().map(SequencingRunResult::platform).map(SequencingPlatform::getGeneration).map(Enum::name).collect(Collectors.joining("|")),
                                                        bestForQuery.sequencingCostInternalNodes.sequencingRunResults().stream().mapToInt(SequencingRunResult::sequenceLength).min().orElseThrow(),
                                                        bestForQuery.sequencingCostInternalNodes.sequencingRunResults().stream().mapToInt(SequencingRunResult::sequenceLength).max().orElseThrow(),
                                                        bestForQuery.sequencingCostInternalNodes.cost(),
                                                        bestForQuery.sequencingCostInternalNodes.cost(),
                                                        bestForQuery.sequencingCostInternalNodes.cost() / bestForQuery.pointersGigaNucleotides,

                                                        bestForQuery.sequencingCostLeafNodes.sequencingRunResults().stream().map(SequencingRunResult::platform).map(SequencingPlatform::getName).collect(Collectors.joining("|")),
                                                        bestForQuery.sequencingCostLeafNodes.sequencingRunResults().stream().map(SequencingRunResult::platform).map(SequencingPlatform::getGeneration).map(Enum::name).collect(Collectors.joining("|")),
                                                        bestForQuery.sequencingCostLeafNodes.sequencingRunResults().stream().mapToInt(SequencingRunResult::sequenceLength).min().orElseThrow(),
                                                        bestForQuery.sequencingCostLeafNodes.sequencingRunResults().stream().mapToInt(SequencingRunResult::sequenceLength).max().orElseThrow(),
                                                        bestForQuery.sequencingCostLeafNodes.cost(),
                                                        bestForQuery.sequencingCostLeafNodes.cost(),
                                                        bestForQuery.sequencingCostLeafNodes.cost() / bestForQuery.objectsGigaNucleotides,

                                                        pointerSize,
                                                        bitrate,
                                                        b,
                                                        c,
                                                        treeHeight,
                                                        numInternalNodes,
                                                        numLeafNodes,
                                                        numPointers,
                                                        numberTuples,
                                                        tupleSize,
                                                        selectivity,
                                                        bufferedInternalNodes,
                                                        bufferedLeafNodes,

                                                        pointersAsGigaNucleotidesRaw,
                                                        objectsAsGigaNucleotidesRaw,
                                                        bestForQuery.pointersGigaNucleotides, // with respect to bitrate
                                                        bestForQuery.objectsGigaNucleotides, // with respect to bitrate

                                                        bestForQuery.numNucleotidesPerInternalNode,
                                                        bestForQuery.numNucleotidesPerLeafNode,

                                                        bestForQuery.querySequencingCost,

                                                        bestForSequenceAll.sequencingCostInternalNodes.sequencingRunResults().stream().map(SequencingRunResult::platform).map(SequencingPlatform::getName).collect(Collectors.joining("|")),
                                                        bestForSequenceAll.sequencingCostInternalNodes.sequencingRunResults().get(0).platform().getGeneration().name(),
                                                        bestForSequenceAll.sequencingCostLeafNodes.sequencingRunResults().stream().map(SequencingRunResult::platform).map(SequencingPlatform::getName).collect(Collectors.joining("|")),
                                                        bestForSequenceAll.sequencingCostLeafNodes.sequencingRunResults().get(0).platform().getGeneration().name(),
                                                        bestForSequenceAll.totalCostReadAll(),
                                                        bestForSequenceAll.totalCostReadAll() - bestForQuery.querySequencingCost
                                                ).map(Object::toString).toArray(String[]::new));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("sequence all stats: ");
        System.out.println("internaNodes:");
        sequenceAllInternalNodesFreqs.forEach((k, v) -> System.out.println("generation: " + k +  " -> "  +  v));
        System.out.println("leafNodes:");
        sequenceAllLeafNodesFreqs.forEach((k, v) -> System.out.println("generation: " + k + " -> " +  v));

        System.out.println("\nsequence query stats: ");
        System.out.println("internaNodes:");
        sequenceQueryInternalNodesFreqs.forEach((k, v) -> System.out.println("generation: " + k +  " -> "  +  v));
        System.out.println("leafNodes:");
        sequenceQueryLeafNodesFreqs.forEach((k, v) -> System.out.println("generation: " + k + " -> " +  v));
        writer.close();
    }

    public record SequencingCombo(
            SequencingResult sequencingCostInternalNodes,
            SequencingResult sequencingCostLeafNodes,
            double pointersGigaNucleotides,
            double objectsGigaNucleotides,
            double numNucleotidesPerInternalNode,
            double sequencingCostPerInternalNode,
            double numNucleotidesPerLeafNode,
            double sequencingCostPerLeafNode,
            double totalCostReadAll,
            double querySequencingCost
    ) {

    }
}
