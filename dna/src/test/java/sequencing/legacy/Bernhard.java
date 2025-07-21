package sequencing.legacy;

import sequencing.utils.Commons;
import utils.FuncUtils;
import utils.Pair;
import utils.csv.BufferedCsvWriter;
import utils.sequencing.Generation;
import utils.sequencing.SequencingPlatform;
import utils.sequencing.SequencingResult;
import java.util.*;
import java.util.stream.Stream;

public class Bernhard {

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

        Map<Generation, List<SequencingPlatform>> gens = new HashMap<>() {{
            put(Generation.FIRST, SANGER);
            put(Generation.SECOND, NGS);
            put(Generation.THIRD, ONT);
        }};


        BufferedCsvWriter writer = new BufferedCsvWriter("D:/Promotion/paper_4/results/new/bernhard_test.csv", false);

        writer.appendNewLine(
                "selectivity",
                "visited internal nodes",
                "visited leaf nodes",
                "tree height",
                "nodes type",
                "b",
                "c",
                "address size",
                "payload size",
                "generation",
                "cost"
        );

        List<Pair<List<SequencingPlatform>, Commons.StrandConfig>> configs = Arrays.asList(
                new Pair<>(NGS, new Commons.StrandConfig(80, 300-80)),
                new Pair<>(SANGER, new Commons.StrandConfig(80, 300-80)),
                new Pair<>(ONT, new Commons.StrandConfig(80, 300-80))
        );
        int pointerSize = 8;
        for (long numberTuples : new long[] {10_000_000_000L}) {
            for (int tupleSize : Arrays.asList(100)) {
                for (int b : Arrays.asList(2, 4, 8, 16, 64, 128, 256, 512, 1024, 2048, 4096)) {
                    int internalNodePointers = 2 * b - 1;
                    for (int c : Arrays.asList(2, 4, 8, 16, 64, 128, 256, 512, 1024, 2048, 4096)) {
                        int leafSize = 2 * c - 1;
                        long numLeafNodes = (int) Math.ceil((double) numberTuples / leafSize);
                        int treeHeight = (int) Math.ceil(Math.log(numLeafNodes) / Math.log(b));
                        long numInternalNodes = Commons.numInternals(b, treeHeight);
                        long numPointers = numInternalNodes > 0 ?
                                numInternalNodes * internalNodePointers // the number of children pointers
                                        + numLeafNodes / (2L * b - 1L) : 0L; // the number of pointers to the nodes to the right (above leaves' level)

                        double objectsAsGigaNucleotidesRaw = (double) numberTuples * tupleSize * 4d / 1000_000_000d;
                        double pointersAsGigaNucleotidesRaw = (double) numPointers * pointerSize * 4d / 1000_000_000d;


                        for (double selectivity : List.of(0.0d, 0.05d, 0.1d, 0.15d, 0.2d, 0.25d, 0.3d, 0.35d, 0.4d, 0.45d, 0.5d, 0.55d, 0.6d, 0.65d, 0.7d, 0.75d, 0.8d, 0.85d, 0.9d, 0.95d, 1.0d)) {
                            Commons.QueryPlan queryEvaluationCost = Commons.visitRange(
                                    numberTuples,
                                    selectivity,
                                    b,
                                    c,
                                    0L,
                                    0L
                            );

                            for (var config1 : configs) {
                                double pointersGigaNucleotides = Commons.totalGigaNts(pointersAsGigaNucleotidesRaw, config1.getT2(), 1.8d, 99.99d, Commons.accuracy(config1.getT1().get(0).getGeneration()));
                                for (var config2 : configs) {
                                    double objectsGigaNucleotides = Commons.totalGigaNts(objectsAsGigaNucleotidesRaw, config2.getT2(), 1.8d, 99.99d, Commons.accuracy(config2.getT1().get(0).getGeneration()));

                                    double internalNodeSize = pointersGigaNucleotides / numInternalNodes;
                                    double leafNodeSize = objectsGigaNucleotides / numLeafNodes;

                                    Double internalNodesSequencingCost = FuncUtils.superSafeCall(() -> sequenceNodes(queryEvaluationCost.visitedInternalNodes(), internalNodeSize, config1.getT2(), config1.getT1()));
                                    if (internalNodesSequencingCost == null) {
                                        System.out.println("failed to sequence internal nodes : " + readable(config1));
                                        continue;
                                    }

                                    Double leafNodesSequencingCost = FuncUtils.superSafeCall(() -> sequenceNodes(queryEvaluationCost.visitedLeafNodes(), leafNodeSize, config2.getT2(), config2.getT1()));
                                    if (leafNodesSequencingCost == null) {
                                        System.out.println("failed to sequence leaf nodes: " + readable(config2));
                                        continue;
                                    }

                                    writer.appendNewLine(
                                            Stream.of(
                                                    selectivity,
                                                            queryEvaluationCost.visitedInternalNodes(),
                                                            queryEvaluationCost.visitedLeafNodes(),
                                                            treeHeight,
                                                            "internal nodes",
                                                            b,
                                                            c,
                                                            config1.getT2().addressSize(),
                                                            config1.getT2().payloadSize(),
                                                            config1.getT1().get(0).getGeneration(),
                                                            internalNodesSequencingCost)
                                                    .map(Objects::toString).toArray(String[]::new)
                                    );
                                    writer.appendNewLine(
                                            Stream.of(
                                                            selectivity,
                                                            queryEvaluationCost.visitedInternalNodes(),
                                                            queryEvaluationCost.visitedLeafNodes(),
                                                            treeHeight,
                                                            "leaf nodes",
                                                            b,
                                                            c,
                                                            config2.getT2().addressSize(),
                                                            config2.getT2().payloadSize(),
                                                            config2.getT1().get(0).getGeneration(),
                                                            leafNodesSequencingCost)
                                                    .map(Objects::toString).toArray(String[]::new)
                                    );
                                }
                            }
                        }
                    }
                }
            }
        }

        writer.close();
    }

    static String readable(Pair<List<SequencingPlatform>, Commons.StrandConfig> pair) {
        return pair.getT1().get(0).getGeneration() + " @ " +  pair.getT2().strandSize();
    }

    static SequencingResult sequence(List<SequencingPlatform> plaforms, Commons.StrandConfig config, double gigaNts) {
        double count = gigaNts * 1000_000_000d / config.payloadSize();
        return plaforms.stream()
                .map(p -> FuncUtils.superSafeCall(() -> p.sequence(count, config.strandSize(), 1)))
                .filter(Objects::nonNull)
                .min(Comparator.comparingDouble(SequencingResult::cost))
                .orElse(null);
    }

    static Double sequenceNodes(double visitedNodes, double nodeGigaSize, Commons.StrandConfig config, List<SequencingPlatform> platforms) {
        SequencingResult sr = sequence(platforms, config, nodeGigaSize);
        return sr.cost() * visitedNodes;
    }
}
