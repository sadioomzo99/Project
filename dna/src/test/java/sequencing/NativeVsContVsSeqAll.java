package sequencing;

import sequencing.utils.Commons;
import sequencing.utils.SheetParser;
import sequencing.utils.sheet.SeqPlat;
import utils.Pair;
import utils.Range;
import utils.csv.BufferedCsvWriter;
import utils.sequencing.Generation;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class NativeVsContVsSeqAll {

    public static void main(String[] args) {
        List<SeqPlat> plats = SheetParser.parseSheet();
        List<Pair<SeqPlat, Commons.StrandConfig>> matches = new ArrayList<>();
        //matches.put(plats.get(6), new Commons.StrandConfig(80, 150-80));
        //matches.put(plats.get(13), new Commons.StrandConfig(80, 150-80));
        //matches.put(plats.get(9), new Commons.StrandConfig(80, 150-80));
        //matches.put(plats.get(10), new Commons.StrandConfig(80, 150-80));
        //var allMatches = plats.stream().filter(p -> Commons.generation(p) == Generation.SECOND || Commons.generation(p) == Generation.THIRD).collect(Collectors.toMap(p -> p, p -> new Commons.StrandConfig(80, p.read().length().max() - 80)));
        var allMatches = plats.stream().filter(p -> Commons.generation(p) == Generation.SECOND || Commons.generation(p) == Generation.THIRD).filter(p -> p.platform().contains("ILMN NovaSeq S2 2fcells") || p.platform().contains("ONT GridION X5 5fcells  Q20+ data with Kit14")).collect(Collectors.toMap(p -> p, p -> new Commons.StrandConfig(80, p.read().length().max() - 80)));

        // 300 -> ILMN MiSeq 1fcell
        // 150 -> ILMN NovaSeq S2 2fcells"
        // ILMN NovaSeq S4 v1.5 2fcells
        matches.add(new Pair<>(plats.get(IntStream.range(0, plats.size()).filter(i -> plats.get(i).platform().contains("ILMN NovaSeq S2 2fcells")).findFirst().orElseThrow()), new Commons.StrandConfig(80, 150-80)));
        matches.add(new Pair<>(plats.get(IntStream.range(0, plats.size()).filter(i -> plats.get(i).platform().contains("ILMN NovaSeq SP 2fcells")).findFirst().orElseThrow()), new Commons.StrandConfig(80, 250-80)));
        matches.add(new Pair<>(plats.get(IntStream.range(0, plats.size()).filter(i -> plats.get(i).platform().contains("ONT GridION X5 5fcells  Q20+ data with Kit14")).findFirst().orElseThrow()), new Commons.StrandConfig(80, 150-80)));
        matches.add(new Pair<>(plats.get(IntStream.range(0, plats.size()).filter(i -> plats.get(i).platform().contains("ONT GridION X5 5fcells  Q20+ data with Kit14")).findFirst().orElseThrow()), new Commons.StrandConfig(80, 1000-80)));
        matches.add(new Pair<>(plats.get(IntStream.range(0, plats.size()).filter(i -> plats.get(i).platform().contains("ONT GridION X5 5fcells  Q20+ data with Kit14")).findFirst().orElseThrow()), new Commons.StrandConfig(80, 10_000-80)));
        matches.add(new Pair<>(plats.get(IntStream.range(0, plats.size()).filter(i -> plats.get(i).platform().contains("ONT GridION X5 5fcells  Q20+ data with Kit14")).findFirst().orElseThrow()), new Commons.StrandConfig(80, 15_000-80)));
        matches.add(new Pair<>(plats.get(IntStream.range(0, plats.size()).filter(i -> plats.get(i).platform().contains("ONT GridION X5 5fcells  Q20+ data with Kit14")).findFirst().orElseThrow()), new Commons.StrandConfig(80, 20_000-80)));
        matches.add(new Pair<>(plats.get(IntStream.range(0, plats.size()).filter(i -> plats.get(i).platform().contains("ONT GridION X5 5fcells  Q20+ data with Kit14")).findFirst().orElseThrow()), new Commons.StrandConfig(80, 60_000-80)));

        matches.add(
                new Pair<>(
                       new SeqPlat(
                               "Future 1",
                               new Range.NumberRange<>(1, 1000_000),
                               new SeqPlat.Read(new Range.NumberRange<>(50, 1000), SeqPlat.ReadConfig.SE),
                               Duration.ofHours(10),
                               new Range.NumberRange<>(50d, 1000_000d * 1000d),
                               50f,
                               new Range.NumberRange<>(50, 500),
                               new Range.NumberRange<>(1f, 10f),
                               new Range.NumberRange<>(0, 1),
                               new Range.NumberRange<>(0, 1),
                               new Range.NumberRange<>(0, 1),
                               new Range.NumberRange<>(100f, 5000f),
                               0,
                               "none",
                               1000_000f * 1000f,
                               0f,
                               0
                       ),
                       new Commons.StrandConfig(80, 1000-80)
                )
        );

        Map<Commons.StrandConfig, Bitrate> bitratesNative = new HashMap<>();
        bitratesNative.put(new Commons.StrandConfig(80, 150-80), new Bitrate(0.7d, 0.7d));
        bitratesNative.put(new Commons.StrandConfig(80, 250-80), new Bitrate(1.108d, 1.08d));
        bitratesNative.put(new Commons.StrandConfig(80, 1000-80), new Bitrate(1.538d, 1.537d));
        bitratesNative.put(new Commons.StrandConfig(80, 10_000-80), new Bitrate(1.598d, 1.602d));
        bitratesNative.put(new Commons.StrandConfig(80, 15_000-80), new Bitrate(1.554d, 1.56d));
        bitratesNative.put(new Commons.StrandConfig(80, 20_000-80), new Bitrate(1.4557d, 1.493d));
        bitratesNative.put(new Commons.StrandConfig(80, 60_000-80), new Bitrate(1.39d, 1.418d));

        double bitrateDiffContainer = 0.15d;


        double tupleSize = 563.039;
        var pointerSize = 8L;

        BufferedCsvWriter writer = new BufferedCsvWriter("D:/Promotion/paper_4/experimentations/native_container_seqall_full_zz.csv", false);
        writer.appendNewLine(
                "selectivity",
                "b",
                "c",
                "height",
                "number tuples",
                "num tuples constant",

                "internals platform",
                "internals generation",
                "internals strand size",

                "leaves platform",
                "leaves generation",
                "leaves strand size",

                "type",

                "internals costs",
                "leaves costs",

                "total costs",

                "internals time",
                "leaves time",

                "total time",

                "cost per gigaNt"
        );

        for (var numTuplesConst : Arrays.asList(true)) {
            for (var selectivity : Arrays.asList(0.99d)) {
            //for (var selectivity : Arrays.asList(0.0d, 0.05d, 0.1d, 0.15d, 0.2d, 0.25d, 0.3d, 0.35d, 0.4d, 0.45d, 0.5d, 0.55d, 0.6d, 0.65d, 0.7d, 0.75d, 0.8d, 0.85d, 0.9d, 0.95d, 0.99d)) {
                for (var internalPlat : matches) {
                    var internalGen = Commons.generation(internalPlat.getT1());
                    for (var leafPlat : matches) {
                        var leafGen = Commons.generation(leafPlat.getT1());
                        for (int b : IntStream.iterate(2, x -> x <= 4096, x -> x * 2).toArray()) {
                            long internalNodeSize = 2L * b - 1L;
                            for (int c : IntStream.iterate(1, x -> x <= 4096, x -> x * 2).toArray()) {
                                long leafSize = 2L * c - 1L;
                                double numberTuples = 1_000_000d;
                                double numLeafNodes = Math.ceil(numberTuples / leafSize);
                                int treeHeight = (int) Math.ceil(Math.log(numLeafNodes) / Math.log(b));

                                if (!numTuplesConst) {
                                    treeHeight = 4;
                                    numberTuples = leafSize * (int) Math.pow(internalNodeSize, treeHeight);
                                    numLeafNodes = Math.ceil(numberTuples / leafSize);
                                }


                                double numInternalNodes = Commons.numInternals(b, treeHeight);
                                double numPointers = numInternalNodes > 0 ?
                                        numInternalNodes * internalNodeSize // the number of children pointers
                                                + numLeafNodes / (2L * b - 1d) : 0d; // the number of pointers to the nodes to the right (above leaves' level)

                                double objectsAsGigaNucleotidesRaw = numberTuples * tupleSize * 4d / 1000_000_000d;
                                double pointersAsGigaNucleotidesRaw = numPointers * pointerSize * 4d / 1000_000_000d;

                                double objectsAsGigaNucleotidesAdjusted = Commons.totalGigaNts(objectsAsGigaNucleotidesRaw, leafPlat.getT2(), 1.7d, 99.99d, Commons.accuracy(leafGen));
                                //double pointersAsGigaNucleotidesAdjusted = Commons.totalGigaNts(pointersAsGigaNucleotidesRaw, internalPlat.getValue(), 2d, 99.99d, Commons.accuracy(internalGen));

                                double objectsAsGigaNucleotidesAdjustedNative = Commons.totalGigaNts(objectsAsGigaNucleotidesRaw, leafPlat.getT2(), bitratesNative.get(leafPlat.getT2()).leaves, 99.99d, Commons.accuracy(leafGen));
                                double pointersAsGigaNucleotidesAdjustedNative = Commons.totalGigaNts(pointersAsGigaNucleotidesRaw, internalPlat.getT2(), bitratesNative.get(internalPlat.getT2()).internals, 99.99d, Commons.accuracy(internalGen));

                                double objectsAsGigaNucleotidesAdjustedContainer = Commons.totalGigaNts(objectsAsGigaNucleotidesRaw, leafPlat.getT2(), bitratesNative.get(leafPlat.getT2()).leaves - bitrateDiffContainer, 99.99d, Commons.accuracy(leafGen));
                                double pointersAsGigaNucleotidesAdjustedContainer = Commons.totalGigaNts(pointersAsGigaNucleotidesRaw, internalPlat.getT2(), bitratesNative.get(internalPlat.getT2()).internals - bitrateDiffContainer, 99.99d, Commons.accuracy(internalGen));

                                Commons.QueryPlan queryPlan = Commons.visitRange(
                                        numberTuples,
                                        selectivity,
                                        b,
                                        c,
                                        0,
                                        0
                                );

                                Commons.QueryPlan queryPlanAdjusted = new Commons.QueryPlan(
                                        Math.ceil(queryPlan.visitedInternalNodes()),
                                        Commons.generation(internalPlat.getT1()) == Generation.THIRD ? queryPlan.visitedLeafNodes() : Math.ceil(queryPlan.visitedLeafNodes())
                                );


                                double internalNodeGigaNtsAdjustedContainer = pointersAsGigaNucleotidesAdjustedContainer / numInternalNodes;
                                double leafNodeGigaNtsAdjustedContainer = objectsAsGigaNucleotidesAdjustedContainer / numLeafNodes;

                                double internalNodeGigaNtsAdjustedNative = pointersAsGigaNucleotidesAdjustedNative / numInternalNodes;
                                double leafNodeGigaNtsAdjustedNative = objectsAsGigaNucleotidesAdjustedNative / numLeafNodes;

                                var costInternalsNative = Commons.sequence(internalPlat.getT1(), internalPlat.getT2(), internalNodeGigaNtsAdjustedNative).multiply(queryPlanAdjusted.visitedInternalNodes());
                                var costLeavesNative = Commons.sequence(leafPlat.getT1(), leafPlat.getT2(), leafNodeGigaNtsAdjustedNative).multiply(queryPlanAdjusted.visitedLeafNodes());
                                var costTotalNative = costInternalsNative.plus(costLeavesNative);

                                //double adjustedInternalCostsNative = queryPlanAdjusted.visitedInternalNodes() * Commons.sequence(internalPlat.getKey(), internalPlat.getValue(), internalNodeGigaNtsAdjusted).cost();
                                //double adjustedLeavesCostsNative = queryPlanAdjusted.visitedLeafNodes() * Commons.sequence(leafPlat.getKey(), leafPlat.getValue(), leafNodeGigaNtsAdjusted).cost();
                                //double adjustedTotalCostsNative = adjustedInternalCostsNative + adjustedLeavesCostsNative;


                                var costInternalsContainer = Commons.sequence(internalPlat.getT1(), internalPlat.getT2(), internalNodeGigaNtsAdjustedContainer).plus(Commons.sequence(internalPlat.getT1(), internalPlat.getT2(), 150d / 1000_000_000d)).multiply(queryPlanAdjusted.visitedInternalNodes());
                                var costLeavesContainer = Commons.sequence(leafPlat.getT1(), leafPlat.getT2(), leafNodeGigaNtsAdjustedContainer).plus(Commons.sequence(leafPlat.getT1(), leafPlat.getT2(), 150d / 1000_000_000d)).multiply(queryPlanAdjusted.visitedLeafNodes());
                                var costTotalContainer = costInternalsContainer.plus(costLeavesContainer);


                                //double adjustedInternalCostsContainer = queryPlanAdjusted.visitedInternalNodes() * (Commons.sequence(internalPlat.getKey(), internalPlat.getValue(), internalNodeGigaNtsAdjusted).cost() + Commons.sequence(internalPlat.getKey(), internalPlat.getValue(), 150d / 1000_000_000d).cost());
                                //double adjustedLeavesCostsContainer  = queryPlanAdjusted.visitedLeafNodes() * (Commons.sequence(leafPlat.getKey(), leafPlat.getValue(), leafNodeGigaNtsAdjusted).cost() + Commons.sequence(leafPlat.getKey(), leafPlat.getValue(), 150d / 1000_000_000d).cost());
                                //double adjustedTotalCostsContainer  = adjustedInternalCostsContainer + adjustedLeavesCostsContainer;

                                if (internalPlat == leafPlat) {
                                    var adjustedTotalCostsSeqAll = Commons.sequence(leafPlat.getT1(), leafPlat.getT2(), objectsAsGigaNucleotidesAdjusted);
                                    // seq all
                                    writer.appendNewLine(
                                            Stream.of(
                                                    selectivity,
                                                    b,
                                                    c,
                                                    treeHeight,
                                                    numberTuples,
                                                    numTuplesConst,

                                                    internalPlat.getT1().platform(),
                                                    internalGen,
                                                    internalPlat.getT2().strandSize(),

                                                    leafPlat.getT1().platform(),
                                                    leafGen,
                                                    leafPlat.getT2().strandSize(),

                                                    "Read all",

                                                    Double.NaN,
                                                    Double.NaN,
                                                    adjustedTotalCostsSeqAll.cost(),

                                                    Double.NaN,
                                                    Double.NaN,

                                                    adjustedTotalCostsSeqAll.hours(),
                                                    adjustedTotalCostsSeqAll.cost() / objectsAsGigaNucleotidesAdjusted
                                            ).map(Objects::toString).toArray(String[]::new)
                                    );
                                }

                                // native
                                writer.appendNewLine(
                                        selectivity,
                                        b,
                                        c,
                                        treeHeight,
                                        numberTuples,
                                        numTuplesConst,

                                        internalPlat.getT1().platform(),
                                        internalGen,
                                        internalPlat.getT2().strandSize(),

                                        leafPlat.getT1().platform(),
                                        leafGen,
                                        leafPlat.getT2().strandSize(),

                                        "Native tree",

                                        costInternalsNative.cost(),
                                        costLeavesNative.cost(),

                                        costTotalNative.cost(),

                                        costInternalsNative.hours(),
                                        costLeavesNative.hours(),

                                        costTotalNative.hours(),

                                        costTotalNative.cost() / (queryPlanAdjusted.visitedInternalNodes() * internalNodeGigaNtsAdjustedNative + queryPlanAdjusted.visitedLeafNodes() * leafNodeGigaNtsAdjustedNative)
                                );

                                // container
                                writer.appendNewLine(
                                        Stream.of(
                                                selectivity,
                                                b,
                                                c,
                                                treeHeight,
                                                numberTuples,
                                                numTuplesConst,

                                                internalPlat.getT1().platform(),
                                                internalGen,
                                                internalPlat.getT2().strandSize(),

                                                leafPlat.getT1().platform(),
                                                leafGen,
                                                leafPlat.getT2().strandSize(),

                                                "Container tree",

                                                costInternalsContainer.cost(),
                                                costLeavesContainer.cost(),

                                                costTotalContainer.cost(),

                                                costInternalsContainer.hours(),
                                                costLeavesContainer.hours(),

                                                costTotalContainer.hours(),

                                                costTotalContainer.cost() / (queryPlanAdjusted.visitedInternalNodes() * (internalNodeGigaNtsAdjustedContainer + 150d / 1000_000_000d) + queryPlanAdjusted.visitedLeafNodes() * (leafNodeGigaNtsAdjustedContainer + 150d / 1000_000_000d))
                                        ).map(Objects::toString).toArray(String[]::new)
                                );

                            }
                        }
                    }
                }
            }
        }

        writer.close();
    }

    public record Bitrate(double internals, double leaves) {

    }
}
