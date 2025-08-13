package sequencing;

import sequencing.utils.Commons;
import utils.FuncUtils;
import utils.csv.BufferedCsvWriter;
import utils.sequencing.SequencingPlatform;
import utils.sequencing.SequencingResult;
import java.util.*;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

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
        configs.put(ONT, new Commons.StrandConfig(80, 60_000 - 80));


        BufferedCsvWriter writer = new BufferedCsvWriter("D:/Promotion/paper_4/experimentations/sequencing_analysis_complex.csv", false);
        writer.appendNewLine(
                "platformInternalNodes_name",
                "platformInternalNodes_generation",

                "platformLeafNodes_name",
                "platformLeafNodes_generation",

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

                "querySequencingCost",
                "totalCostReadAll"
        );

        long pointerSize = 100;
        double pointersBitrate = 2d;
        for (float bitrate : List.of(2f)) {
            for (int b : List.of(2, 16, 32, 64, 128, 256, 512)) {
                int internalNodePointers = 2 * b - 1;
                for (int c : List.of(2, 16, 32, 64, 128, 256, 512)) {
                    int leafSize = 2 * c - 1;
                    for (long numberTuples : List.of(10_000_000_000L)) {
                        for (long tupleSize : List.of(100L)) {
                            for (double selectivity : List.of(0.001d, 0.01d, 0.1d, 0.2d, 0.5d, 0.95d)) {
                                long numLeafNodes = (int) Math.ceil((double) numberTuples / leafSize);
                                long numInternalNodes = Commons.numInternals(b, (int) (Math.log(numLeafNodes) / Math.log(b)));

                                for (int bufferedInternalNodes : DoubleStream.of(0d).mapToInt(factor -> (int) (Math.ceil(factor * numInternalNodes))).toArray()) {
                                    for (int bufferedLeafNodes : DoubleStream.of(0d).mapToInt(factor -> (int) (Math.ceil(factor * numLeafNodes))).toArray()) {
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
                                        for (List<SequencingPlatform> platformInternalNodes : platforms) {
                                            double pointersGigaNucleotides = Commons.totalGigaNts(pointersAsGigaNucleotidesRaw, configs.get(platformInternalNodes), pointersBitrate, 99.99d, Commons.accuracy(platformInternalNodes.get(0).getGeneration()));
                                            for (List<SequencingPlatform> platformLeafNodes : platforms) {
                                                double objectsGigaNucleotides = Commons.totalGigaNts(objectsAsGigaNucleotidesRaw, configs.get(platformInternalNodes), bitrate, 99.99d, Commons.accuracy(platformLeafNodes.get(0).getGeneration()));

                                                double numGigaNucleotidesPerInternalNode = numInternalNodes > 0 ? pointersGigaNucleotides / numInternalNodes : 0d;
                                                double numGigaNucleotidesPerLeafNode =  objectsGigaNucleotides / numLeafNodes;


                                                SequencingResult sequencingCostSingleInternalNode = platformInternalNodes.stream().map(p -> FuncUtils.superSafeCall(() -> p.sequence(numGigaNucleotidesPerInternalNode / configs.get(platformInternalNodes).payloadSize(), configs.get(platformInternalNodes).strandSize(), 1))).filter(Objects::nonNull).min(Comparator.comparingDouble(SequencingResult::cost)).orElse(null);
                                                SequencingResult sequencingCostSingleLeafNode = platformLeafNodes.stream().map(p -> FuncUtils.superSafeCall(() -> p.sequence(numGigaNucleotidesPerLeafNode / configs.get(platformLeafNodes).payloadSize(), configs.get(platformLeafNodes).strandSize(), 1))).filter(Objects::nonNull).min(Comparator.comparingDouble(SequencingResult::cost)).orElse(null);

                                                if (sequencingCostSingleInternalNode == null || sequencingCostSingleLeafNode == null)
                                                    continue;

                                                if (sequencingCostSingleInternalNode.sequencingRunResults() == null || sequencingCostSingleLeafNode.sequencingRunResults() == null)
                                                    continue;

                                                if (sequencingCostSingleInternalNode.sequencingRunResults().isEmpty() || sequencingCostSingleLeafNode.sequencingRunResults().isEmpty())
                                                    continue;

                                                var sequencingCostInternalNodes = numInternalNodes * sequencingCostSingleInternalNode.cost();
                                                var sequencingCostLeafNodes = queryEvaluationCost.visitedLeafNodes() * sequencingCostSingleLeafNode.cost();
                                                double totalCostReadAll = sequencingCostInternalNodes + numLeafNodes * sequencingCostSingleLeafNode.cost();
                                                double querySequencingCost = sequencingCostInternalNodes + sequencingCostLeafNodes;

                                                writer.appendNewLine(
                                                        Stream.of(
                                                                sequencingCostSingleInternalNode.sequencingRunResults().get(0).platform().getName(),
                                                                sequencingCostSingleInternalNode.sequencingRunResults().get(0).platform().getGeneration().name(),

                                                                sequencingCostSingleLeafNode.sequencingRunResults().get(0).platform().getName(),
                                                                sequencingCostSingleLeafNode.sequencingRunResults().get(0).platform().getGeneration().name(),

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

                                                                querySequencingCost,
                                                                totalCostReadAll
                                                        ).map(Object::toString).toArray(String[]::new));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        writer.close();
    }

    public record SequencingCombo(
            double sequencingCostInternalNodes,
            double sequencingCostLeafNodes,
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
