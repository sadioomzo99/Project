package sequencing.legacy;

import sequencing.NativeVsContVsSeqAll;
import sequencing.utils.Commons;
import sequencing.utils.SheetParser;
import sequencing.utils.sheet.SeqPlat;
import utils.Pair;
import utils.csv.BufferedCsvWriter;
import utils.sequencing.Generation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class AmotorizedTest {

    public static void main(String[] args) {
        List<SeqPlat> plats = SheetParser.parseSheet();
        Map<Commons.StrandConfig, NativeVsContVsSeqAll.Bitrate> bitratesNative = new HashMap<>();
        bitratesNative.put(new Commons.StrandConfig(80, 150-80), new NativeVsContVsSeqAll.Bitrate(0.7d, 0.7d));
        bitratesNative.put(new Commons.StrandConfig(80, 250-80), new NativeVsContVsSeqAll.Bitrate(1.108d, 1.08d));
        bitratesNative.put(new Commons.StrandConfig(80, 1000-80), new NativeVsContVsSeqAll.Bitrate(1.538d, 1.537d));
        bitratesNative.put(new Commons.StrandConfig(80, 10_000-80), new NativeVsContVsSeqAll.Bitrate(1.598d, 1.602d));
        bitratesNative.put(new Commons.StrandConfig(80, 15_000-80), new NativeVsContVsSeqAll.Bitrate(1.554d, 1.56d));
        bitratesNative.put(new Commons.StrandConfig(80, 20_000-80), new NativeVsContVsSeqAll.Bitrate(1.4557d, 1.493d));
        bitratesNative.put(new Commons.StrandConfig(80, 60_000-80), new NativeVsContVsSeqAll.Bitrate(1.39d, 1.418d));

        List<Pair<SeqPlat, Commons.StrandConfig>> matches = new ArrayList<>();

        matches.add(new Pair<>(plats.get(IntStream.range(0, plats.size()).filter(i -> plats.get(i).platform().contains("ILMN NovaSeq S2 2fcells")).findFirst().orElseThrow()), new Commons.StrandConfig(80, 150-80)));
        matches.add(new Pair<>(plats.get(IntStream.range(0, plats.size()).filter(i -> plats.get(i).platform().contains("ILMN NovaSeq SP 2fcells")).findFirst().orElseThrow()), new Commons.StrandConfig(80, 250-80)));
        matches.add(new Pair<>(plats.get(IntStream.range(0, plats.size()).filter(i -> plats.get(i).platform().contains("ONT GridION X5 5fcells  Q20+ data with Kit14")).findFirst().orElseThrow()), new Commons.StrandConfig(80, 150-80)));
        matches.add(new Pair<>(plats.get(IntStream.range(0, plats.size()).filter(i -> plats.get(i).platform().contains("ONT GridION X5 5fcells  Q20+ data with Kit14")).findFirst().orElseThrow()), new Commons.StrandConfig(80, 1000-80)));
        matches.add(new Pair<>(plats.get(IntStream.range(0, plats.size()).filter(i -> plats.get(i).platform().contains("ONT GridION X5 5fcells  Q20+ data with Kit14")).findFirst().orElseThrow()), new Commons.StrandConfig(80, 10_000-80)));
        matches.add(new Pair<>(plats.get(IntStream.range(0, plats.size()).filter(i -> plats.get(i).platform().contains("ONT GridION X5 5fcells  Q20+ data with Kit14")).findFirst().orElseThrow()), new Commons.StrandConfig(80, 15_000-80)));
        matches.add(new Pair<>(plats.get(IntStream.range(0, plats.size()).filter(i -> plats.get(i).platform().contains("ONT GridION X5 5fcells  Q20+ data with Kit14")).findFirst().orElseThrow()), new Commons.StrandConfig(80, 20_000-80)));
        matches.add(new Pair<>(plats.get(IntStream.range(0, plats.size()).filter(i -> plats.get(i).platform().contains("ONT GridION X5 5fcells  Q20+ data with Kit14")).findFirst().orElseThrow()), new Commons.StrandConfig(80, 60_000-80)));



        BufferedCsvWriter writer = new BufferedCsvWriter("D:/Promotion/paper_4/results/new/amotorized_analysis_t_t.csv", false);
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
                "total cost",
                "synthesis cost",
                "trys",
                "amotorized cost"
        );

        long pointerSize = 4L;
        double pointersBitrate = 2d;
        double dollarsPerNt = 0.0005d;
        for (Bitrate bitrate : List.of(
                new Bitrate(70, 1.7d, 0.721151d, 0.6421171),
                new Bitrate(250, 1.7d, 1.1082608d, 0.9930812d),
                new Bitrate(1_000, 1.7d, 1.5380145d, 1.3836005d),
                new Bitrate(10_000, 1.7d, 1.5985689d, 1.4322257d),
                new Bitrate(15_000, 1.7d, 1.5545664d, 1.4150927d),
                new Bitrate(20_000, 1.7d, 1.4857032d, 1.3947997d),
                new Bitrate(60_000, 1.7d, 1.3974718d, 1.2620766d)
        )) {
            for (int b : List.of(64)) {
                int internalNodePointers = 2 * b - 1;
                for (int c : List.of(64)) {
                    int leafSize = 2 * c - 1;
                    for (long numberTuples : List.of(10_000L)) {
                        for (double tupleSize : List.of(563.039d)) {
                            for (double selectivity : List.of(0.5d, 0.6d, 0.7d, 0.8d, 0.9d, 0.99d)) {
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

                                        for (int trys : IntStream.iterate(0, x -> x <= 50_000, x -> x + 1000).toArray()) {
                                            long numPointers = numInternalNodes > 0 ?
                                                    numInternalNodes * internalNodePointers // the number of children pointers
                                                            + numLeafNodes / (2L * b - 1L) : 0L; // the number of pointers to the nodes to the right (above leaves' level)

                                            double objectsAsGigaNucleotidesRaw = (double) numberTuples * tupleSize * 4d / 1000_000_000d;
                                            double pointersAsGigaNucleotidesRaw = (double) numPointers * pointerSize * 4d / 1000_000_000d;

                                            // raw reading costs
                                            SeqPlat minPlat = null;
                                            double objectsAsGigaNucleotidesRawBitrated = 2d / bitrate.bitrateRaw * objectsAsGigaNucleotidesRaw;

                                            double minCost = Double.MAX_VALUE;
                                            for (var plat : matches) {
                                                var cost = Commons.sequence(plat.getT1(), plat.getT2(), objectsAsGigaNucleotidesRawBitrated).cost();
                                                if (cost < minCost) {
                                                    minCost = cost;
                                                    minPlat = plat.getT1();
                                                }
                                            }


                                            double synthesisCost = dollarsPerNt * objectsAsGigaNucleotidesRawBitrated * 1000_000_000d;

                                            double amotorizedCost = synthesisCost + trys * minCost;
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
                                                    Commons.generation(minPlat),
                                                    Commons.generation(minPlat),
                                                    0.0d,
                                                    minCost,
                                                    synthesisCost,
                                                    trys,
                                                    amotorizedCost
                                            );





                                            minCost = Double.MAX_VALUE;
                                            SeqPlat minPlatInternals = null;
                                            SeqPlat minPlatLeaves = null;
                                            double minPointersGigaNucleotides = Double.MAX_VALUE;
                                            double minObjectsGigaNucleotides = Double.MAX_VALUE;
                                            double minNumGigaNucleotidesPerInternalNode = Double.MAX_VALUE;
                                            double minNumGigaNucleotidesPerLeafNode = Double.MAX_VALUE;
                                            double minSequencingCostPerInternalNode = Double.MAX_VALUE;
                                            double minSequencingCostPerLeafNode = Double.MAX_VALUE;


                                            // Native tree's reading costs
                                            for (var platformInternalNodes : matches) {
                                                double pointersGigaNucleotides = Commons.totalGigaNts(pointersAsGigaNucleotidesRaw, platformInternalNodes.getT2(), bitrate.bitrateNativeTree, 99.99d, Commons.accuracy(Commons.generation(platformInternalNodes.getT1())));
                                                for (var platformLeafNodes : matches) {
                                                    double objectsGigaNucleotides = Commons.totalGigaNts(objectsAsGigaNucleotidesRaw, platformLeafNodes.getT2(), bitrate.bitrateNativeTree, 99.99d, Commons.accuracy(Commons.generation(platformLeafNodes.getT1())));

                                                    double sequencingCostInternalNodes = Commons.sequence(platformInternalNodes.getT1(), platformInternalNodes.getT2(), pointersGigaNucleotides).cost();
                                                    double sequencingCostLeafNodes =  Commons.sequence(platformLeafNodes.getT1(), platformLeafNodes.getT2(), objectsGigaNucleotides).cost();

                                                    double numGigaNucleotidesPerInternalNode = numInternalNodes > 0 ? pointersGigaNucleotides / numInternalNodes : 0d;
                                                    double sequencingCostPerInternalNode = numInternalNodes > 0 ? sequencingCostInternalNodes / numInternalNodes : 0d;

                                                    double numGigaNucleotidesPerLeafNode = objectsGigaNucleotides / numLeafNodes;
                                                    double sequencingCostPerLeafNode = sequencingCostLeafNodes / numLeafNodes;

                                                    double totalCost = queryEvaluationCost.visitedLeafNodes() * sequencingCostPerLeafNode + queryEvaluationCost.visitedInternalNodes() * sequencingCostPerInternalNode;
                                                    if (totalCost < minCost) {
                                                        synthesisCost = dollarsPerNt * (objectsGigaNucleotides + pointersGigaNucleotides) * 1000_000_000d;
                                                        minCost = totalCost;
                                                        minPlatInternals = platformInternalNodes.getT1();
                                                        minPlatLeaves = platformLeafNodes.getT1();

                                                        minPointersGigaNucleotides = pointersGigaNucleotides;
                                                        minObjectsGigaNucleotides = objectsGigaNucleotides;
                                                        minNumGigaNucleotidesPerInternalNode = numGigaNucleotidesPerInternalNode;
                                                        minNumGigaNucleotidesPerLeafNode = numGigaNucleotidesPerLeafNode;

                                                        minSequencingCostPerInternalNode = sequencingCostPerInternalNode;
                                                        minSequencingCostPerLeafNode = sequencingCostPerLeafNode;
                                                    }
                                                }
                                            }

                                            amotorizedCost = synthesisCost + trys * minCost;
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
                                                    Commons.generation(minPlatInternals),
                                                    Commons.generation(minPlatLeaves),
                                                    queryEvaluationCost.visitedInternalNodes() * minSequencingCostPerInternalNode,
                                                    queryEvaluationCost.visitedLeafNodes() * minSequencingCostPerLeafNode,
                                                    synthesisCost,
                                                    trys,
                                                    amotorizedCost
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
                                            for (var platformInternalNodes : matches) {
                                                double pointersGigaNucleotides = Commons.totalGigaNts(pointersAsGigaNucleotidesRaw, platformInternalNodes.getT2(), bitrate.bitrateContainerTree, 99.99d, Commons.accuracy(Commons.generation(platformInternalNodes.getT1())));
                                                for (var platformLeafNodes : matches) {
                                                    double objectsGigaNucleotides = Commons.totalGigaNts(objectsAsGigaNucleotidesRaw, platformInternalNodes.getT2(), bitrate.bitrateContainerTree, 99.99d, Commons.accuracy(Commons.generation(platformLeafNodes.getT1())));

                                                    double sequencingCostInternalNodes = Commons.sequence(platformInternalNodes.getT1(), platformInternalNodes.getT2(), pointersGigaNucleotides).cost();
                                                    double sequencingCostLeafNodes = Commons.sequence(platformLeafNodes.getT1(), platformLeafNodes.getT2(), objectsGigaNucleotides).cost();

                                                    double numGigaNucleotidesPerInternalNode = numInternalNodes > 0 ? pointersGigaNucleotides / numInternalNodes : 0d;
                                                    double sequencingCostPerInternalNode = numInternalNodes > 0 ? sequencingCostInternalNodes / numInternalNodes : 0d;

                                                    double numGigaNucleotidesPerLeafNode = objectsGigaNucleotides / numLeafNodes;
                                                    double sequencingCostPerLeafNode = sequencingCostLeafNodes / numLeafNodes;

                                                    double totalCost = queryEvaluationCost.visitedLeafNodes() * sequencingCostPerLeafNode + queryEvaluationCost.visitedInternalNodes() * sequencingCostPerInternalNode;
                                                    if (totalCost < minCost) {
                                                        synthesisCost = dollarsPerNt * (objectsGigaNucleotides + pointersGigaNucleotides) * 1000_000_000d;
                                                        minCost = totalCost;
                                                        minPlatInternals = platformInternalNodes.getT1();
                                                        minPlatLeaves = platformLeafNodes.getT1();

                                                        minPointersGigaNucleotides = pointersGigaNucleotides;
                                                        minObjectsGigaNucleotides = objectsGigaNucleotides;
                                                        minNumGigaNucleotidesPerInternalNode = numGigaNucleotidesPerInternalNode;
                                                        minNumGigaNucleotidesPerLeafNode = numGigaNucleotidesPerLeafNode;

                                                        minSequencingCostPerInternalNode = sequencingCostPerInternalNode;
                                                        minSequencingCostPerLeafNode = sequencingCostPerLeafNode;
                                                    }
                                                }
                                            }

                                            amotorizedCost = synthesisCost + trys * minCost;
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
                                                    Commons.generation(minPlatInternals),
                                                    Commons.generation(minPlatLeaves),
                                                    queryEvaluationCost.visitedInternalNodes() * minSequencingCostPerInternalNode,
                                                    queryEvaluationCost.visitedLeafNodes() * minSequencingCostPerLeafNode,
                                                    synthesisCost,
                                                    trys,
                                                    amotorizedCost
                                            );
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

    private static void write(
            int oligoSize,
            double bitrate,
            int b,
            int c,
            long numberTuples,
            double tupleSize,
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
            double costLeafNodes,
            double synthesisCost,
            int trys,
            double amotorizedCost
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
                        costInternalNodes + costLeafNodes,
                        synthesisCost,
                        trys,
                        amotorizedCost
                ).map(Object::toString).toArray(String[]::new));
    }

    record Bitrate(int oligoSize, double bitrateRaw, double bitrateNativeTree, double bitrateContainerTree) {

    }
}
