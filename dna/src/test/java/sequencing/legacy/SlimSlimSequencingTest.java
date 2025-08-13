package sequencing.legacy;

import sequencing.utils.Commons;
import utils.Pair;
import utils.Range;
import utils.csv.BufferedCsvWriter;
import utils.sequencing.Generation;
import utils.sequencing.SimpleSequencingPlatform;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public class SlimSlimSequencingTest {

    public static void main(String[] args) {
        BufferedCsvWriter writer = new BufferedCsvWriter("D:/Promotion/paper_4/results/new/sequencing_analysis.csv", false);
        writer.appendNewLine(
                "oligoSize",
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

                "pointersGigaNucleotidesByBitrate", // with respect to bitrate
                "objectsGigaNucleotidesByBitrate", // with respect to bitrate

                "numGigaNucleotidesPerInternalNode",
                "numGigaNucleotidesPerLeafNode",

                "Sequencing type", // all or query

                "internalNodes Sequencing",
                "leafNodes Sequencing",
                "SequencingCombo",
                "cost internal nodes",
                "cost leaf nodes",
                "total cost"
        );

        long pointerSize = 4L;
        double pointersBitrate = 2d;
        for (Bitrate bitrate : List.of(
                new Bitrate(70, 1.6d, 0.721151d, 0.6421171),
                new Bitrate(250, 1.6d, 1.1082608d, 0.9930812d),
                new Bitrate(1_000, 1.6d, 1.5380145d, 1.3836005d),
                new Bitrate(10_000, 1.6d, 1.5985689d, 1.4322257d),
                new Bitrate(15_000, 1.6d, 1.5545664d, 1.4150927d),
                new Bitrate(20_000, 1.6d, 1.4857032d, 1.3947997d),
                new Bitrate(60_000, 1.6d, 1.3974718d, 1.2620766d)
        )) {
            for (int b : List.of(64)) {
                int internalNodePointers = 2 * b - 1;
                for (int c : List.of(64)) {
                    int leafSize = 2 * c - 1;
                    for (long numberTuples : List.of(10_000_000L)) {
                        for (long tupleSize : List.of(576L)) {
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

                                        // raw reading costs
                                        SimpleSequencingPlatform minPlat = null;
                                        double objectsAsGigaNucleotidesRawBitrated = 2d / bitrate.bitrateRaw * objectsAsGigaNucleotidesRaw;

                                        double minCost = Double.MAX_VALUE;
                                        for (var plat : Commons.SIMPLE_PLATFORMS)
                                            if (plat.sequence(objectsAsGigaNucleotidesRawBitrated).average().doubleValue() < minCost) {
                                                minCost = plat.sequence(objectsAsGigaNucleotidesRawBitrated).average().doubleValue();
                                                minPlat = plat;
                                            }

                                        write(
                                                bitrate.oligoSize,
                                                bitrate.bitrateRaw,
                                                b,
                                                c,
                                                numberTuples,
                                                tupleSize,
                                                selectivity,
                                                bufferedInternalNodes,
                                                bufferedLeafNodes,
                                                writer,
                                                pointerSize,
                                                -1,
                                                numInternalNodes,
                                                numLeafNodes,
                                                numPointers,
                                                Double.NaN,
                                                objectsAsGigaNucleotidesRaw,
                                                Double.NaN,
                                                objectsAsGigaNucleotidesRaw,
                                                Double.NaN,
                                                Double.NaN,
                                                Commons.SequencingType.ALL_RAW,
                                                minPlat.generation(),
                                                minPlat.generation(),
                                                0.0d,
                                                minCost
                                        );


                                        minCost = Double.MAX_VALUE;
                                        SimpleSequencingPlatform minPlatInternals = null;
                                        SimpleSequencingPlatform minPlatLeaves = null;
                                        double minPointersGigaNucleotides = Double.MAX_VALUE;
                                        double minObjectsGigaNucleotides = Double.MAX_VALUE;
                                        double minNumGigaNucleotidesPerInternalNode = Double.MAX_VALUE;
                                        double minNumGigaNucleotidesPerLeafNode = Double.MAX_VALUE;
                                        double minSequencingCostPerInternalNode = Double.MAX_VALUE;
                                        double minSequencingCostPerLeafNode = Double.MAX_VALUE;


                                        // Native tree's reading costs
                                        for (SimpleSequencingPlatform platformInternalNodes : Commons.SIMPLE_PLATFORMS) {
                                            double pointersGigaNucleotides = Commons.totalGigaNts(pointersAsGigaNucleotidesRaw, Commons.CONFIGS.get(platformInternalNodes), bitrate.bitrateNativeTree, 99.99d, platformInternalNodes.readAccuracy().average().doubleValue());
                                            for (SimpleSequencingPlatform platformLeafNodes : Commons.SIMPLE_PLATFORMS) {
                                                double objectsGigaNucleotides = Commons.totalGigaNts(objectsAsGigaNucleotidesRaw, Commons.CONFIGS.get(platformInternalNodes), bitrate.bitrateNativeTree, 99.99d, platformLeafNodes.readAccuracy().average().doubleValue());

                                                Range.NumberRange<Double> sequencingCostInternalNodes = platformInternalNodes.sequence(pointersGigaNucleotides);
                                                Range.NumberRange<Double> sequencingCostLeafNodes = platformLeafNodes.sequence(objectsGigaNucleotides);

                                                double numGigaNucleotidesPerInternalNode = numInternalNodes > 0 ? pointersGigaNucleotides / numInternalNodes : 0d;
                                                double sequencingCostPerInternalNode = numInternalNodes > 0 ? sequencingCostInternalNodes.average().doubleValue() / numInternalNodes : 0d;

                                                double numGigaNucleotidesPerLeafNode = objectsGigaNucleotides / numLeafNodes;
                                                double sequencingCostPerLeafNode = sequencingCostLeafNodes.average().doubleValue() / numLeafNodes;

                                                double totalCost = queryEvaluationCost.visitedLeafNodes() * sequencingCostPerLeafNode + queryEvaluationCost.visitedInternalNodes() * sequencingCostPerInternalNode;
                                                if (totalCost < minCost) {
                                                    minCost = totalCost;
                                                    minPlatInternals = platformInternalNodes;
                                                    minPlatLeaves = platformLeafNodes;

                                                    minPointersGigaNucleotides = pointersGigaNucleotides;
                                                    minObjectsGigaNucleotides = objectsGigaNucleotides;
                                                    minNumGigaNucleotidesPerInternalNode = numGigaNucleotidesPerInternalNode;
                                                    minNumGigaNucleotidesPerLeafNode = numGigaNucleotidesPerLeafNode;

                                                    minSequencingCostPerInternalNode = sequencingCostPerInternalNode;
                                                    minSequencingCostPerLeafNode = sequencingCostPerLeafNode;
                                                }
                                            }
                                        }

                                        write(
                                                bitrate.oligoSize,
                                                bitrate.bitrateNativeTree,
                                                b,
                                                c,
                                                numberTuples,
                                                tupleSize,
                                                selectivity,
                                                bufferedInternalNodes,
                                                bufferedLeafNodes,
                                                writer,
                                                pointerSize,
                                                treeHeight,
                                                numInternalNodes,
                                                numLeafNodes,
                                                numPointers,
                                                pointersAsGigaNucleotidesRaw,
                                                objectsAsGigaNucleotidesRaw,
                                                minPointersGigaNucleotides,
                                                minObjectsGigaNucleotides,
                                                minNumGigaNucleotidesPerInternalNode,
                                                minNumGigaNucleotidesPerLeafNode,
                                                Commons.SequencingType.QUERY_NATIVE,
                                                minPlatInternals.generation(),
                                                minPlatLeaves.generation(),
                                                queryEvaluationCost.visitedInternalNodes() * minSequencingCostPerInternalNode,
                                                queryEvaluationCost.visitedLeafNodes() * minSequencingCostPerLeafNode
                                        );



                                        minCost = Double.MAX_VALUE;
                                        minPlatInternals = null;
                                        minPlatLeaves = null;
                                        minPointersGigaNucleotides = Double.MAX_VALUE;
                                        minObjectsGigaNucleotides = Double.MAX_VALUE;
                                        minNumGigaNucleotidesPerInternalNode = Double.MAX_VALUE;
                                        minNumGigaNucleotidesPerLeafNode = Double.MAX_VALUE;
                                        minSequencingCostPerInternalNode = Double.MAX_VALUE;
                                        minSequencingCostPerLeafNode = Double.MAX_VALUE;

                                        // DNAContainer's reading costs
                                        for (SimpleSequencingPlatform platformInternalNodes : Commons.SIMPLE_PLATFORMS) {
                                            double pointersGigaNucleotides = Commons.totalGigaNts(pointersAsGigaNucleotidesRaw, Commons.CONFIGS.get(platformInternalNodes), bitrate.bitrateContainerTree, 99.99d, platformInternalNodes.readAccuracy().average().doubleValue());
                                            for (SimpleSequencingPlatform platformLeafNodes : Commons.SIMPLE_PLATFORMS) {
                                                double objectsGigaNucleotides = Commons.totalGigaNts(objectsAsGigaNucleotidesRaw, Commons.CONFIGS.get(platformInternalNodes), bitrate.bitrateContainerTree, 99.99d, platformLeafNodes.readAccuracy().average().doubleValue());

                                                Range.NumberRange<Double> sequencingCostInternalNodes = platformInternalNodes.sequence(pointersGigaNucleotides);
                                                Range.NumberRange<Double> sequencingCostLeafNodes = platformLeafNodes.sequence(objectsGigaNucleotides);

                                                double numGigaNucleotidesPerInternalNode = numInternalNodes > 0 ? pointersGigaNucleotides / numInternalNodes : 0d;
                                                double sequencingCostPerInternalNode = numInternalNodes > 0 ? sequencingCostInternalNodes.average().doubleValue() / numInternalNodes : 0d;

                                                double numGigaNucleotidesPerLeafNode = objectsGigaNucleotides / numLeafNodes;
                                                double sequencingCostPerLeafNode = sequencingCostLeafNodes.average().doubleValue() / numLeafNodes;

                                                double totalCost = queryEvaluationCost.visitedLeafNodes() * sequencingCostPerLeafNode + queryEvaluationCost.visitedInternalNodes() * sequencingCostPerInternalNode;
                                                if (totalCost < minCost) {
                                                    minCost = totalCost;
                                                    minPlatInternals = platformInternalNodes;
                                                    minPlatLeaves = platformLeafNodes;

                                                    minPointersGigaNucleotides = pointersGigaNucleotides;
                                                    minObjectsGigaNucleotides = objectsGigaNucleotides;
                                                    minNumGigaNucleotidesPerInternalNode = numGigaNucleotidesPerInternalNode;
                                                    minNumGigaNucleotidesPerLeafNode = numGigaNucleotidesPerLeafNode;

                                                    minSequencingCostPerInternalNode = sequencingCostPerInternalNode;
                                                    minSequencingCostPerLeafNode = sequencingCostPerLeafNode;
                                                }
                                            }
                                        }

                                        write(
                                                bitrate.oligoSize,
                                                bitrate.bitrateContainerTree,
                                                b,
                                                c,
                                                numberTuples,
                                                tupleSize,
                                                selectivity,
                                                bufferedInternalNodes,
                                                bufferedLeafNodes,
                                                writer,
                                                pointerSize,
                                                treeHeight,
                                                numInternalNodes,
                                                numLeafNodes,
                                                numPointers,
                                                pointersAsGigaNucleotidesRaw,
                                                objectsAsGigaNucleotidesRaw,
                                                minPointersGigaNucleotides,
                                                minObjectsGigaNucleotides,
                                                minNumGigaNucleotidesPerInternalNode,
                                                minNumGigaNucleotidesPerLeafNode,
                                                Commons.SequencingType.QUERY_DNA_CONTAINER,
                                                minPlatInternals.generation(),
                                                minPlatLeaves.generation(),
                                                queryEvaluationCost.visitedInternalNodes() * minSequencingCostPerInternalNode,
                                                queryEvaluationCost.visitedLeafNodes() * minSequencingCostPerLeafNode
                                        );
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

    private static void write(
            int oligoSize,
            double bitrate,
            int b,
            int c,
            long numberTuples,
            long tupleSize,
            double selectivity,
            Pair<Double, Double> bufferedInternalNodes,
            Pair<Double, Double> bufferedLeafNodes,
            BufferedCsvWriter writer,
            long pointerSize,
            int treeHeight,
            long numInternalNodes,
            long numLeafNodes,
            long numPointers,
            double pointersAsGigaNucleotidesRaw,
            double objectsAsGigaNucleotidesRaw,
            double pointersAsGigaNucleotidesByBitrate,
            double objectsAsGigaNucleotidesByBitrate,
            double numGigaNucleotidesPerInternalNode,
            double numGigaNucleotidesPerLeafNode,
            Commons.SequencingType sequencingType,
            Generation internalNodesSequencingGeneration,
            Generation leafNodesSequencingGeneration,
            double costInternalNodes,
            double costLeafNodes
    ) {
        writer.appendNewLine(
                Stream.of(
                        oligoSize,
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

                        pointersAsGigaNucleotidesByBitrate,
                        objectsAsGigaNucleotidesByBitrate,

                        numGigaNucleotidesPerInternalNode,
                        numGigaNucleotidesPerLeafNode,

                        sequencingType.name(),

                        internalNodesSequencingGeneration.name(),
                        leafNodesSequencingGeneration.name(),
                        internalNodesSequencingGeneration.name() + " + " + leafNodesSequencingGeneration.name(),
                        costInternalNodes,
                        costLeafNodes,
                        costInternalNodes + costLeafNodes
                ).map(Object::toString).toArray(String[]::new));
    }

    record Bitrate(int oligoSize, double bitrateRaw, double bitrateNativeTree, double bitrateContainerTree) {

    }
}
