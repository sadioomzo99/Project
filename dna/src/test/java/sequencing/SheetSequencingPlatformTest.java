package sequencing;

import sequencing.utils.Commons;
import sequencing.utils.SheetParser;
import sequencing.utils.sheet.SeqPlat;
import utils.csv.BufferedCsvWriter;
import utils.sequencing.Generation;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SheetSequencingPlatformTest {

    public static void main(String[] args) {
        List<SeqPlat> plats = SheetParser.parseSheet();
        Map<SeqPlat, Commons.StrandConfig> matches = new HashMap<>();
        //matches.put(plats.get(6), new Commons.StrandConfig(80, 150-80));
        //matches.put(plats.get(13), new Commons.StrandConfig(80, 150-80));
        //matches.put(plats.get(9), new Commons.StrandConfig(80, 150-80));
        //matches.put(plats.get(10), new Commons.StrandConfig(80, 150-80));
        //var allMatches = plats.stream().filter(p -> Commons.generation(p) == Generation.SECOND || Commons.generation(p) == Generation.THIRD).collect(Collectors.toMap(p -> p, p -> new Commons.StrandConfig(80, p.read().length().max() - 80)));
        var allMatches = plats.stream().filter(p -> Commons.generation(p) == Generation.SECOND || Commons.generation(p) == Generation.THIRD).filter(p -> p.platform().contains("ILMN NovaSeq S2 2fcells") || p.platform().contains("ONT GridION X5 5fcells  Q20+ data with Kit14")).collect(Collectors.toMap(p -> p, p -> new Commons.StrandConfig(80, p.read().length().max() - 80)));

        matches.put(plats.get(11), new Commons.StrandConfig(80, 150-80));
        matches.put(plats.get(6), new Commons.StrandConfig(80, 150-80));
        matches.put(plats.get(30), new Commons.StrandConfig(80, 10_000-80));


        var tupleSize = 100L;
        var pointerSize = 8L;

        BufferedCsvWriter writer = new BufferedCsvWriter("D:/Promotion/paper_4/experimentations/corrected.csv", false);
        writer.appendNewLine(
                "selectivity",
                "b",
                "c",
                "height",
                "number tuples",
                "constant number tuples",

                "internals platform",
                "internals generation",
                "internals strand size",

                "leaves platform",
                "leaves generation",
                "leaves strand size",

                "plain internals costs",
                "plain leaves costs",

                "adjusted internals costs",
                "adjusted leaves costs",

                "plain total costs",
                "adjusted total costs",

                "plain cost per gigaNt",
                "adjusted cost per gigaNt"
        );

        for (boolean constantNumTuples : Arrays.asList(true, false)) {
            for (var selectivity : Arrays.asList(0.99d)) {
                for (var internalPlat : allMatches.entrySet()) {
                    var internalGen = Commons.generation(internalPlat.getKey());
                    for(var leafPlat : allMatches.entrySet()) {
                        var leafGen = Commons.generation(leafPlat.getKey());
                        for (int b : IntStream.iterate(2, x -> x <= 10_000, x -> x == 2 ? 500 : x + 500).toArray()) {
                            long internalNodeSize = 2L * b - 1L;
                            for (int c : IntStream.iterate(2, x -> x <= 10_000, x -> x == 2 ? 500 : x + 500).toArray()) {
                                long leafSize = 2L * c - 1L;
                                for (int h = 1; h < 10; h++) {
                                    double numberTuples = constantNumTuples ? 1_000_000_000d : leafSize * Math.pow(internalNodeSize, h);
                                    double numLeafNodes = Math.ceil(numberTuples / leafSize);
                                    int treeHeight = h;
                                    if (constantNumTuples) {
                                        treeHeight = (int) Math.ceil(Math.log(numLeafNodes) / Math.log(b));
                                        h = Integer.MAX_VALUE;
                                    }


                                    double numInternalNodes = Commons.numInternals(b, treeHeight);
                                    double numPointers = numInternalNodes > 0 ?
                                            numInternalNodes * internalNodeSize // the number of children pointers
                                                    + numLeafNodes / (2L * b - 1d) : 0d; // the number of pointers to the nodes to the right (above leaves' level)

                                    double objectsAsGigaNucleotidesRaw = numberTuples * tupleSize * 4d / 1000_000_000d;
                                    double pointersAsGigaNucleotidesRaw = numPointers * pointerSize * 4d / 1000_000_000d;

                                    double objectsAsGigaNucleotidesAdjusted = Commons.totalGigaNts(objectsAsGigaNucleotidesRaw, leafPlat.getValue(), 2d, 99.99d, Commons.accuracy(leafGen));
                                    double pointersAsGigaNucleotidesAdjusted = Commons.totalGigaNts(pointersAsGigaNucleotidesRaw, internalPlat.getValue(), 2d, 99.99d, Commons.accuracy(internalGen));

                                    Commons.QueryPlan queryPlan = Commons.visitRange(
                                            numberTuples,
                                            selectivity,
                                            b,
                                            c,
                                            0,
                                            0
                                    );

                                    Commons.QueryPlan queryPlanAdjusted = new Commons.QueryPlan(
                                            Commons.generation(internalPlat.getKey()) == Generation.THIRD ? queryPlan.visitedInternalNodes() : Math.ceil(queryPlan.visitedInternalNodes()),
                                            Commons.generation(internalPlat.getKey()) == Generation.THIRD ? queryPlan.visitedLeafNodes() : Math.ceil(queryPlan.visitedLeafNodes())
                                    );

                                    double internalNodeGigaNtsRaw = pointersAsGigaNucleotidesRaw/ numInternalNodes;
                                    double internalNodeGigaNtsAdjusted = pointersAsGigaNucleotidesAdjusted/ numInternalNodes;

                                    double leafNodeGigaNtsRaw = objectsAsGigaNucleotidesRaw/ numLeafNodes;
                                    double leafNodeGigaNtsAdjusted = objectsAsGigaNucleotidesAdjusted/ numLeafNodes;

                                    double plainInternalCosts = queryPlanAdjusted.visitedInternalNodes() * Commons.sequence(internalPlat.getKey(), internalPlat.getValue(), internalNodeGigaNtsRaw).cost();
                                    double plainLeavesCosts = queryPlanAdjusted.visitedLeafNodes() * Commons.sequence(leafPlat.getKey(), leafPlat.getValue(), leafNodeGigaNtsRaw).cost();
                                    double plainTotalCost = plainInternalCosts + plainLeavesCosts;


                                    double adjustedInternalCosts = queryPlanAdjusted.visitedInternalNodes() * Commons.sequence(internalPlat.getKey(), internalPlat.getValue(), internalNodeGigaNtsAdjusted).cost();
                                    double adjustedLeavesCosts = queryPlanAdjusted.visitedLeafNodes() * Commons.sequence(leafPlat.getKey(), leafPlat.getValue(), leafNodeGigaNtsAdjusted).cost();
                                    double adjustedTotalCost = adjustedInternalCosts + adjustedLeavesCosts;

                                    writer.appendNewLine(
                                            Stream.of(
                                                    selectivity,
                                                    b,
                                                    c,
                                                    treeHeight,
                                                    numberTuples,
                                                    constantNumTuples,

                                                    internalPlat.getKey().platform(),
                                                    internalGen,
                                                    internalPlat.getValue().strandSize(),

                                                    leafPlat.getKey().platform(),
                                                    leafGen,
                                                    leafPlat.getValue().strandSize(),

                                                    plainInternalCosts,
                                                    plainLeavesCosts,

                                                    adjustedInternalCosts,
                                                    adjustedLeavesCosts,

                                                    plainTotalCost,
                                                    adjustedTotalCost,

                                                    plainTotalCost / (queryPlanAdjusted.visitedInternalNodes() * internalNodeGigaNtsRaw + queryPlanAdjusted.visitedLeafNodes() * leafNodeGigaNtsRaw),
                                                    adjustedTotalCost / (queryPlanAdjusted.visitedInternalNodes() * internalNodeGigaNtsAdjusted + queryPlanAdjusted.visitedLeafNodes() * leafNodeGigaNtsAdjusted)

                                            ).map(Objects::toString).toArray(String[]::new)
                                    );
                                }
                            }
                        }
                    }
                }
            }
        }



        writer.close();

        /*
        BufferedCsvWriter writer = new BufferedCsvWriter("D:/Promotion/paper_4/experimentations/experimentation.csv", false);
        writer.appendNewLine(
                "platform",
                "gigaNts",
                "strand size",
                "sequencing",
                "cost"
        );

        for (var e : matches.entrySet()) {
            for (double gigaNts = 1d; gigaNts < 1000d; gigaNts+=10d) {
                writer.appendNewLine(
                        Stream.of(
                                e.getKey().platform(),
                                gigaNts,
                                e.getValue().strandSize(),
                                BernhardSheet.generation(e.getKey()),
                                BernhardSheet.sequence(e.getKey(), e.getValue(), gigaNts)
                        ).map(Objects::toString).toArray(String[]::new)
                );
            }
        }

        writer.close();

         */
    }
}
