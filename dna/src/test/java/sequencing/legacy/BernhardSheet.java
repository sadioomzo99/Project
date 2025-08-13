package sequencing.legacy;

import sequencing.utils.Commons;
import sequencing.utils.SheetParser;
import sequencing.utils.sheet.SeqPlat;
import utils.Pair;
import utils.csv.BufferedCsvWriter;
import utils.sequencing.Generation;
import java.util.*;
import java.util.stream.Stream;

public class BernhardSheet {

    public static void main(String[] args) {
        List<SeqPlat> plats = SheetParser.parseSheet();

        BufferedCsvWriter writer = new BufferedCsvWriter("D:/Promotion/paper_4/results/new/bernhard_sheet.csv", false);

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
                "strand size",
                "generation",
                "cost"
        );

        List<Commons.StrandConfig> configs = Arrays.asList(
                new Commons.StrandConfig(80, 300-80),
                //new Commons.StrandConfig(80, 250-80),
                //new Commons.StrandConfig(80, 150-80),

                //new Commons.StrandConfig(80, 400-80),
                //new Commons.StrandConfig(80, 600-80),

                //new Commons.StrandConfig(80, 1000-80),
                new Commons.StrandConfig(80, 10_000-80),
                new Commons.StrandConfig(80, 100_000-80)
                //new Commons.StrandConfig(80, 1000_000-80

        );

        List<Pair<Commons.StrandConfig, List<SeqPlat>>> allMatches = configs.stream()
                .map(c -> new Pair<>(c, plats.stream().filter(plat -> Commons.getCompatability(plat, c, 1d) != SeqCompatability.NONE).toList())).toList();

        List<Pair<Commons.StrandConfig, List<SeqPlat>>> matches = Arrays.asList(
                new Pair<>(new Commons.StrandConfig(80, 300-80), plats.stream().filter(plat -> Commons.generation(plat) == Generation.SECOND).findFirst().map(Arrays::asList).orElseThrow()),
                new Pair<>(new Commons.StrandConfig(80, 60000-80), plats.stream().filter(plat -> Commons.generation(plat) == Generation.THIRD).findFirst().map(Arrays::asList).orElseThrow())
        );

        int pointerSize = 4;
        for (int numberTuples : new int[] {1_000_000}) {
            for (int tupleSize : Arrays.asList(100)) {
                for (int b : Arrays.asList(2, 4, 8, 16, 64, 128, 256, 512, 1024, 2048)) {
                    int internalNodePointers = 2 * b - 1;
                    for (int c : Arrays.asList(2, 4, 8, 16, 64, 128, 256, 512, 1024, 2048)) {
                        int leafSize = 2 * c - 1;
                        long numLeafNodes = (int) Math.ceil((double) numberTuples / leafSize);
                        int treeHeight = (int) Math.ceil(Math.log(numLeafNodes) / Math.log(b));
                        long numInternalNodes = Commons.numInternals(b, treeHeight);
                        long numPointers = numInternalNodes > 0 ?
                                numInternalNodes * internalNodePointers // the number of children pointers
                                        + numLeafNodes / (2L * b - 1L) : 0L; // the number of pointers to the nodes to the right (above leaves' level)

                        double objectsAsGigaNucleotidesRaw = (double) numberTuples * tupleSize * 4d / 1000_000_000d;
                        double pointersAsGigaNucleotidesRaw = (double) numPointers * pointerSize * 4d / 1000_000_000d;


                        for (double selectivity : List.of(0.95d)) {
                            Commons.QueryPlan queryEvaluationCost = Commons.visitRange(
                                    numberTuples,
                                    selectivity,
                                    b,
                                    c,
                                    0L,
                                    0L
                            );


                            for (var m1 : matches) {
                                for (var plat1 : m1.getT2()) {
                                    var plat1Gen = Commons.generation(m1.getT2().get(0));
                                    double pointersGigaNucleotides = Commons.totalGigaNts(pointersAsGigaNucleotidesRaw, m1.getT1(), 1.8d, 99.99d, Commons.accuracy(plat1Gen));
                                    for (var m2 : matches) {
                                        for (var plat2 : m2.getT2()) {
                                            var plat2Gen = Commons.generation(m2.getT2().get(0));
                                            double objectsGigaNucleotides = Commons.totalGigaNts(objectsAsGigaNucleotidesRaw, m2.getT1(), 1.8d, 99.99d, Commons.accuracy(plat2Gen));

                                            double internalNodeSize = pointersGigaNucleotides / numInternalNodes;
                                            double leafNodeSize = objectsGigaNucleotides / numLeafNodes;

                                            Double internalNodesSequencingCost = sequenceNodes(queryEvaluationCost.visitedInternalNodes(), internalNodeSize, m1.getT1(), plat1);
                                            Double leafNodesSequencingCost = sequenceNodes(queryEvaluationCost.visitedLeafNodes(), leafNodeSize, m2.getT1(), plat2);

                                            writer.appendNewLine(
                                                    Stream.of(
                                                                    selectivity,
                                                                    queryEvaluationCost.visitedInternalNodes(),
                                                                    queryEvaluationCost.visitedLeafNodes(),
                                                                    treeHeight,
                                                                    "internal nodes",
                                                                    b,
                                                                    c,
                                                                    m1.getT1().addressSize(),
                                                                    m1.getT1().payloadSize(),
                                                                    m1.getT1().strandSize(),
                                                                    plat1Gen,
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
                                                                    m2.getT1().addressSize(),
                                                                    m2.getT1().payloadSize(),
                                                                    m2.getT1().strandSize(),
                                                                    plat2Gen,
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
            }
        }

        writer.close();
    }

    public static double sequence(SeqPlat plat, Commons.StrandConfig config, double gigaNts) {
        var comp = Commons.getCompatability(plat, config, gigaNts);
        if (comp == SeqCompatability.NONE)
            return Double.NaN;

        double millionReads = Math.max(1, gigaNts * 1000d / config.payloadSize());
        if (plat.millionReads() != null) {
            double maxReads = plat.millionReads().max().doubleValue() / (plat.read().config() == SeqPlat.ReadConfig.PE ? 2d : 1d);
            double c = millionReads / maxReads;
            double priceRangeSize = plat.pricePerGbp().max().doubleValue() - plat.pricePerGbp().min().doubleValue();
            var p = plat.pricePerGbp().max().doubleValue() - c * priceRangeSize;
            if (c <= 1d)
                return p * gigaNts;

            var cFloored = Math.floor(c);
            var cRest = c - cFloored;

            p = plat.pricePerGbp().max().doubleValue() - cRest * priceRangeSize;
            return cFloored/c * gigaNts * plat.pricePerGbp().min().doubleValue() + cRest/c * gigaNts * p;
        }

        return plat.pricePerGbp().average().doubleValue() * gigaNts;
    }

    public static Double sequenceNodes(double visitedNodes, double nodeGigaSize, Commons.StrandConfig config, SeqPlat plat) {
        return sequence(plat, config, nodeGigaSize) * visitedNodes;
    }

    public enum SeqCompatability {
        NONE, LENGTH_ONLY, FULL;
    }
}
