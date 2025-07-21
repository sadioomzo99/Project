package sequencing.legacy;

import sequencing.utils.Commons;
import utils.Pair;
import utils.Range;
import utils.csv.BufferedCsvWriter;
import utils.sequencing.Generation;
import utils.sequencing.SimpleSequencingPlatform;
import java.util.*;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public class SlimSequencingPlatformTest {

    public static void main(String[] args) {
        BufferedCsvWriter writer = new BufferedCsvWriter("sequencing_analysis_slim.csv", false);
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
                "bufferedInternalNodesFactor",
                "bufferedLeafNodesFactor",

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
        long pointerSize = 8L;
        long rows = 0L;
        double pointersBitrate = 2d;
        for (double bitrate : List.of(2d)) {
            for (int b : List.of(64)) {
                int internalNodePointers = 2 * b - 1;
                for (int c : List.of(64)) {
                    int leafSize = 2 * c - 1;
                    for (long numberTuples : List.of(10_000_000_000L)) {
                        for (long tupleSize : List.of(100L)) {
                            for (double selectivity : List.of(0.0d, 0.05d, 0.1d, 0.15d, 0.2d, 0.25d, 0.3d, 0.35d, 0.4d, 0.45d, 0.5d, 0.55d, 0.6d, 0.65d, 0.7d, 0.75d, 0.8d, 0.85d, 0.9d, 0.95d, 1.0d)) {
                                long numLeafNodes = (int) Math.ceil((double) numberTuples / leafSize);
                                int treeHeight = (int) Math.ceil(Math.log(numLeafNodes) / Math.log(b));
                                long numInternalNodes = Commons.numInternals(b, treeHeight);

                                for (var bufferedInternalNodes : DoubleStream.of(0d).mapToObj(factor -> new Pair<>(Math.ceil(factor * numInternalNodes), factor)).toList()) {
                                    for (var bufferedLeafNodes : DoubleStream.of(0d).mapToObj(factor -> new Pair<>(Math.ceil(factor * numLeafNodes), factor)).toList()) {
                                        Commons.QueryPlan queryEvaluationCost = Commons.visitRange(
                                                numberTuples,
                                                selectivity,
                                                b,
                                                c,
                                                bufferedInternalNodes.getT1().longValue(),
                                                bufferedLeafNodes.getT1().longValue()
                                        );

                                        long numPointers = numInternalNodes > 0 ?
                                                numInternalNodes * internalNodePointers // the number of children pointers
                                                        + numLeafNodes / (2L * b - 1L) : 0L; // the number of pointers to the nodes to the right (above leaves' level)

                                        double objectsAsGigaNucleotidesRaw = (double) numberTuples * tupleSize * 4d / 1000_000_000d;
                                        double pointersAsGigaNucleotidesRaw = (double) numPointers * pointerSize * 4d / 1000_000_000d;

                                        List<Commons.SequencingCombo> combis = new ArrayList<>(Commons.SIMPLE_PLATFORMS.size() * Commons.SIMPLE_PLATFORMS.size());
                                        for (SimpleSequencingPlatform platformInternalNodes : Commons.SIMPLE_PLATFORMS) {
                                            double pointersGigaNucleotides = Commons.totalGigaNts(pointersAsGigaNucleotidesRaw, Commons.CONFIGS.get(platformInternalNodes), pointersBitrate, 99.99d, platformInternalNodes.readAccuracy().average().doubleValue());
                                            for (SimpleSequencingPlatform platformLeafNodes : Commons.SIMPLE_PLATFORMS) {
                                                double objectsGigaNucleotides = Commons.totalGigaNts(objectsAsGigaNucleotidesRaw, Commons.CONFIGS.get(platformInternalNodes), bitrate, 99.99d, platformLeafNodes.readAccuracy().average().doubleValue());

                                                rows++;
                                                Range.NumberRange<Double> sequencingCostInternalNodes = platformInternalNodes.sequence(pointersGigaNucleotides);
                                                Range.NumberRange<Double> sequencingCostLeafNodes = platformLeafNodes.sequence(objectsGigaNucleotides);

                                                double numGigaNucleotidesPerInternalNode = numInternalNodes > 0 ? pointersGigaNucleotides / numInternalNodes : 0d;
                                                double sequencingCostPerInternalNode = numInternalNodes > 0 ? sequencingCostInternalNodes.average().doubleValue() / numInternalNodes : 0d;

                                                double numGigaNucleotidesPerLeafNode =  objectsGigaNucleotides / numLeafNodes;
                                                double sequencingCostPerLeafNode = sequencingCostLeafNodes.average().doubleValue() / numLeafNodes;

                                                //double totalCostReadAll = sequencingCostInternalNodes.average().doubleValue() + sequencingCostLeafNodes.average().doubleValue();
                                                double totalCostReadAll = Commons.NGS.sequence(objectsAsGigaNucleotidesRaw).average().doubleValue();

                                                double querySequencingCost = queryEvaluationCost.visitedInternalNodes() * sequencingCostPerInternalNode + queryEvaluationCost.visitedLeafNodes() * sequencingCostPerLeafNode;
                                                combis.add(
                                                        new Commons.SequencingCombo(
                                                                platformInternalNodes,
                                                                platformLeafNodes,
                                                                pointersGigaNucleotides,
                                                                objectsGigaNucleotides,
                                                                sequencingCostInternalNodes,
                                                                sequencingCostLeafNodes,
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


                                        Commons.SequencingCombo bestForQuery = combis.stream().min(Comparator.comparingDouble(combo -> combo.querySequencingCost())).orElseThrow();
                                        Commons.SequencingCombo bestForSequenceAll = combis.stream().min(Comparator.comparingDouble(combo -> combo.totalCostReadAll())).orElseThrow();

                                        if (bufferedInternalNodes.getT1().longValue() == 0L && bufferedLeafNodes.getT1().longValue() == 0L) {
                                            sequenceAllInternalNodesFreqs.merge(bestForSequenceAll.platformInternalNodes().generation(), 1L, Long::sum);
                                            sequenceAllLeafNodesFreqs.merge(bestForSequenceAll.platformLeafNodes().generation(), 1L, Long::sum);

                                            sequenceQueryInternalNodesFreqs.merge(bestForQuery.platformInternalNodes().generation(), 1L, Long::sum);
                                            sequenceQueryLeafNodesFreqs.merge(bestForQuery.platformLeafNodes().generation(), 1L, Long::sum);
                                        }



                                        writer.appendNewLine(
                                                Stream.of(
                                                        bestForQuery.platformInternalNodes().name(),
                                                        bestForQuery.platformInternalNodes().generation().name(),
                                                        bestForQuery.platformInternalNodes().readLength().min(),
                                                        bestForQuery.platformInternalNodes().readLength().max(),
                                                        bestForQuery.platformInternalNodes().costPerGigaBase().min(),
                                                        bestForQuery.platformInternalNodes().costPerGigaBase().max(),
                                                        bestForQuery.platformInternalNodes().costPerGigaBase().average().doubleValue(),

                                                        bestForQuery.platformLeafNodes().name(),
                                                        bestForQuery.platformLeafNodes().generation().name(),
                                                        bestForQuery.platformLeafNodes().readLength().min(),
                                                        bestForQuery.platformLeafNodes().readLength().max(),
                                                        bestForQuery.platformLeafNodes().costPerGigaBase().min(),
                                                        bestForQuery.platformLeafNodes().costPerGigaBase().max(),
                                                        bestForQuery.platformLeafNodes().costPerGigaBase().average().doubleValue(),

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
                                                        bufferedInternalNodes.getT2(),
                                                        bufferedLeafNodes.getT2(),

                                                        pointersAsGigaNucleotidesRaw,
                                                        objectsAsGigaNucleotidesRaw,
                                                        bestForQuery.pointersGigaNucleotides(), // with respect to bitrate
                                                        bestForQuery.objectsGigaNucleotides(), // with respect to bitrate

                                                        bestForQuery.numNucleotidesPerInternalNode(),
                                                        bestForQuery.numNucleotidesPerLeafNode(),

                                                        bestForQuery.querySequencingCost(),

                                                        bestForSequenceAll.platformInternalNodes().name(),
                                                        bestForSequenceAll.platformInternalNodes().generation().name(),
                                                        bestForSequenceAll.platformLeafNodes().name(),
                                                        bestForSequenceAll.platformLeafNodes().generation().name(),
                                                        bestForSequenceAll.totalCostReadAll(),
                                                        bestForSequenceAll.totalCostReadAll() - bestForQuery.querySequencingCost()
                                                ).map(Object::toString).toArray(String[]::new));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        System.out.println("#rows: " + rows);
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
}
