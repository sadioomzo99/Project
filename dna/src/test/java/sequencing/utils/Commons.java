package sequencing.utils;

import sequencing.legacy.BernhardSheet;
import sequencing.utils.sheet.SeqPlat;
import utils.Range;
import utils.sequencing.Generation;
import utils.sequencing.SimpleSequencingPlatform;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Commons {

    public static boolean USE_NGS_GOV_PRICE = false;

    public static final int ADDRESS_SIZE = 80;
    public static final int OLIGO_SIZE_SANGER = 1000;
    public static final int OLIGO_SIZE_NGS = 300;
    public static final int OLIGO_SIZE_PAC_BIO = 20_000;
    public static final int OLIGO_SIZE_ONT = 60_000;

    public final static SimpleSequencingPlatform SANGER = new SimpleSequencingPlatform(
            "Sanger",
            Generation.FIRST,
            new Range.NumberRange<>(20, 1000),
            new Range.NumberRange<>(13_000.00d, 13_000.00d),
            new Range.NumberRange<>(99.9f, 100.0f)
    );


    public final static SimpleSequencingPlatform NGS_PAPER = new SimpleSequencingPlatform(
            "NGS_Paper",
            Generation.SECOND,
            new Range.NumberRange<>(75, 300),
            new Range.NumberRange<>(50.00d, 63.00d),
            new Range.NumberRange<>(99.9f, 100.0f)
    );



    // https://view.officeapps.live.com/op/view.aspx?src=https%3A%2F%2Fwww.genome.gov%2Fsites%2Fdefault%2Ffiles%2Fmedia%2Ffiles%2F2023-05%2FSequencing_Cost_Data_Table_May2022.xls&wdOrigin=BROWSELINK
    public final static SimpleSequencingPlatform NGS_GOV = new SimpleSequencingPlatform(
            "NGS_Gov",
            Generation.SECOND,
            new Range.NumberRange<>(75, 300),
            new Range.NumberRange<>(0.006d, 0.175d),
            new Range.NumberRange<>(99.9f, 100.0f)
    );



    public final static SimpleSequencingPlatform PAC_BIO = new SimpleSequencingPlatform(
            "PacBio",
            Generation.PAC_BIO,
            new Range.NumberRange<>(10_000, 20_000),
            new Range.NumberRange<>(43.00d, 86.00d),
            new Range.NumberRange<>(99.0f, 100.0f)
    );

    public final static SimpleSequencingPlatform ONT = new SimpleSequencingPlatform(
            "ONT",
            Generation.THIRD,
            new Range.NumberRange<>(10_000, 60_000),
            new Range.NumberRange<>(21.00d, 43.00d),
            new Range.NumberRange<>(87.0f, 98.0f)
    );

    public static SimpleSequencingPlatform NGS = USE_NGS_GOV_PRICE ? NGS_GOV : NGS_PAPER;

    public final static List<SimpleSequencingPlatform> SIMPLE_PLATFORMS = new ArrayList<>(List.of(SANGER, NGS, PAC_BIO, ONT));

    public final static Map<SimpleSequencingPlatform, Commons.StrandConfig> CONFIGS = new HashMap<>();

    static {
        CONFIGS.put(SANGER, new Commons.StrandConfig(ADDRESS_SIZE, OLIGO_SIZE_SANGER - ADDRESS_SIZE));
        CONFIGS.put(NGS_PAPER, new Commons.StrandConfig(ADDRESS_SIZE, OLIGO_SIZE_NGS - ADDRESS_SIZE));
        CONFIGS.put(NGS_GOV, new Commons.StrandConfig(ADDRESS_SIZE, OLIGO_SIZE_NGS - ADDRESS_SIZE));
        CONFIGS.put(PAC_BIO, new Commons.StrandConfig(ADDRESS_SIZE, OLIGO_SIZE_PAC_BIO - ADDRESS_SIZE));
        CONFIGS.put(ONT, new Commons.StrandConfig(ADDRESS_SIZE, OLIGO_SIZE_ONT - ADDRESS_SIZE));
    }

    public static int numInternals(int b, int h) {
        if (h == 0)
            return 0;

        int fullNodeSize = b == 1 ? 2 : 2 * b - 1;
        return (int) (Math.pow(fullNodeSize, h) - 1) / (fullNodeSize - 1);
    }

    public static double totalGigaNts(double gigaNts, StrandConfig config) {
        return gigaNts / config.payloadSize() * config.strandSize();
    }

    public static double totalGigaNts(double gigaNts, StrandConfig config, double bitrate) {
        var gigaNtsBitrated = gigaNts * 2d / bitrate;
        return gigaNtsBitrated / config.payloadSize() * config.strandSize();
    }

    public static double totalGigaNts(double gigaNts, StrandConfig config, double bitrate, double targetAccuracy, double achievedAccuracy) {
        var gigaNtsBitrated = gigaNts * 2d / bitrate;
        if (targetAccuracy > achievedAccuracy)
            gigaNtsBitrated *= targetAccuracy / achievedAccuracy;

        return gigaNtsBitrated / config.payloadSize() * config.strandSize();
    }

    public static QueryPlan visitRange(double numObjects, double selectivity, int b, int c, double bufferedInternalNode, double bufferedLeafNodes) {
        int leafSize = 2 * c - 1;
        int internalNodeBranchingDegree = 2 * b - 1;

        double numLeafNodes = Math.ceil(numObjects / leafSize);
        if (bufferedLeafNodes > numLeafNodes)
            throw new RuntimeException("#buffered leaf nodes > #leaf nodes");

        double numInternalNodes = numLeafNodes / (b - 1d);
        if (bufferedInternalNode > numInternalNodes)
            throw new RuntimeException("#buffered internal nodes > #internal nodes");

        double height = Math.floor(Math.log(numLeafNodes) / Math.log(b));

        double queriedObjects = Math.ceil((1d - selectivity) * numObjects);
        double visitedLeafNodes = Math.ceil(queriedObjects / leafSize);
        visitedLeafNodes -= visitedLeafNodes * bufferedLeafNodes / numLeafNodes;
        double visitedInternalNodes;
        if (numInternalNodes > 0) {
            visitedInternalNodes = (height - 1) + Math.ceil(visitedLeafNodes / internalNodeBranchingDegree);
            visitedInternalNodes -= visitedInternalNodes * bufferedInternalNode / numInternalNodes;
        }
        else
            visitedInternalNodes = 0;

        return new QueryPlan(
                visitedInternalNodes,
                visitedLeafNodes
        );
    }

    public static QueryPlan visitPoint(long numObjects, int b, int c, long bufferedInternalNode, long bufferedLeafNodes) {
        long numLeafNodes = (long) Math.ceil((double) numObjects / c);
        if (bufferedLeafNodes > numLeafNodes)
            throw new RuntimeException("#buffered leaf nodes > #leaf nodes");

        long numInternalNodes = numLeafNodes / (b - 1);
        if (bufferedInternalNode > numInternalNodes)
            throw new RuntimeException("#buffered internal nodes > #internal nodes");

        int height = (int) Math.ceil(Math.log(numLeafNodes) / Math.log(b));

        float hitBuffedLeafNodeProb = (float) bufferedLeafNodes / numLeafNodes;
        float visitedLeafNodes = 1.0f - hitBuffedLeafNodeProb;

        float visitedInternalNodes = height * visitedLeafNodes;
        visitedInternalNodes -= visitedInternalNodes * (float) bufferedInternalNode / numInternalNodes;

        return new QueryPlan(
                visitedInternalNodes,
                visitedLeafNodes
        );
    }

    public static Generation generation(SeqPlat plat) {
        if (plat.platform().contains("ILMN"))
            return Generation.SECOND;
        if (plat.platform().contains("PACB"))
            return Generation.PAC_BIO;
        if (plat.platform().contains("ONT"))
            return Generation.THIRD;

        return Generation.OTHER;
    }

    public static BernhardSheet.SeqCompatability getCompatability(SeqPlat plat, Commons.StrandConfig config, double gigaNts) {
        if (plat.read() == null || plat.pricePerGbp() == null)
            return BernhardSheet.SeqCompatability.NONE;

        if (!plat.read().length().intersects(config.strandSize()))
            return BernhardSheet.SeqCompatability.NONE;

        if (plat.millionReads() == null)
            return BernhardSheet.SeqCompatability.FULL;

        int millionReads = (int) (gigaNts * 1000d / config.payloadSize());
        return plat.millionReads().max() >= millionReads ? BernhardSheet.SeqCompatability.FULL : BernhardSheet.SeqCompatability.LENGTH_ONLY;
    }

    public static SheetSeqResult sequence(SeqPlat plat, Commons.StrandConfig config, double gigaNts) {
        var comp = getCompatability(plat, config, gigaNts);
        if (comp == BernhardSheet.SeqCompatability.NONE)
            return null;

        //double minCost = plat.read().config() == SeqPlat.ReadConfig.SE ? plat.yield().min() * plat.pricePerGbp().min() : plat.yield().min() * plat.pricePerGbp().min() * 2d;
        double minCost = plat.pricePerGbp().min();
        Duration time;
        double millionReads = Math.max(1, gigaNts * 1000d / config.payloadSize());
        if (plat.millionReads() != null) {
            double maxReads = plat.millionReads().max().doubleValue() / (plat.read().config() == SeqPlat.ReadConfig.PE ? 2d : 1d);
            double c = millionReads / maxReads;
            double priceRangeSize = plat.pricePerGbp().max().doubleValue() - plat.pricePerGbp().min().doubleValue();
            var p = plat.pricePerGbp().max().doubleValue() - c * priceRangeSize;
            var cFloored = Math.floor(c);
            time = plat.runTime().multipliedBy((long) cFloored + (c > cFloored ? 1L : 0L));
            if (c <= 1d)
                return new SheetSeqResult(Math.max(minCost, p * gigaNts), time);


            var cRest = c - cFloored;
            p = plat.pricePerGbp().max().doubleValue() - cRest * priceRangeSize;
            var cost =  cFloored/c * gigaNts * plat.pricePerGbp().min().doubleValue() + Math.max(minCost, cRest/c * gigaNts * p);
            return new SheetSeqResult(cost, time);
        }

        return new SheetSeqResult(plat.pricePerGbp().average().doubleValue() * gigaNts, null);
    }

    public static SheetSeqResult sequenceLegacy(SeqPlat plat, Commons.StrandConfig config, double gigaNts) {
        var comp = getCompatability(plat, config, gigaNts);
        if (comp == BernhardSheet.SeqCompatability.NONE)
            return null;

        //double minCost = plat.read().config() == SeqPlat.ReadConfig.SE ? plat.yield().min() * plat.pricePerGbp().min() : plat.yield().min() * plat.pricePerGbp().min() / 2d;
        Duration time;
        double millionReads = Math.max(1, gigaNts * 1000d / config.payloadSize());
        if (plat.millionReads() != null) {
            double maxReads = plat.millionReads().max().doubleValue() / (plat.read().config() == SeqPlat.ReadConfig.PE ? 2d : 1d);
            double c = millionReads / maxReads;
            double priceRangeSize = plat.pricePerGbp().max().doubleValue() - plat.pricePerGbp().min().doubleValue();
            var p = plat.pricePerGbp().max().doubleValue() - c * priceRangeSize;
            var cFloored = Math.floor(c);
            time = plat.runTime().multipliedBy((long) cFloored + (c > cFloored ? 1L : 0L));
            if (c <= 1d)
                return new SheetSeqResult(p * gigaNts, time);


            var cRest = c - cFloored;
            p = plat.pricePerGbp().max().doubleValue() - cRest * priceRangeSize;
            var cost =  cFloored/c * gigaNts * plat.pricePerGbp().min().doubleValue() + cRest/c * gigaNts * p;
            return new SheetSeqResult(cost, time);
        }

        return new SheetSeqResult(plat.pricePerGbp().average().doubleValue() * gigaNts, null);
    }

    public static double accuracy(Generation gen) {
        return switch (gen) {
            case FIRST -> 99.99;
            case SECOND -> 98.0;
            case THIRD -> 90.0;
            case PAC_BIO -> 99.0;
            case OTHER -> Double.NaN;
        };
    }

    public record StrandConfig(int addressSize, int payloadSize) {
        public int strandSize() {
            return addressSize + payloadSize;
        }
    }

    public record QueryPlan(double visitedInternalNodes, double visitedLeafNodes) {

    }

    public enum SequencingType {
        ALL_RAW, ALL_TREE, QUERY_NATIVE, QUERY_DNA_CONTAINER
    }

    public record SequencingCombo(
            SimpleSequencingPlatform platformInternalNodes,
            SimpleSequencingPlatform platformLeafNodes,
            double pointersGigaNucleotides,
            double objectsGigaNucleotides,
            Range.NumberRange<Double> sequencingCostInternalNodes,
            Range.NumberRange<Double> sequencingCostLeafNodes,
            double numNucleotidesPerInternalNode,
            double sequencingCostPerInternalNode,
            double numNucleotidesPerLeafNode,
            double sequencingCostPerLeafNode,
            double totalCostReadAll,
            double querySequencingCost
    ) {

    }

    public record SheetSeqResult(double cost, Duration time) {

        public Double hours() {
            return time == null? Double.NaN : time.toHours() + time.toMinutesPart() / 60d;
        }

        public SheetSeqResult plus(SheetSeqResult result) {
            return new SheetSeqResult(
                    cost + result.cost,
                    time != null? time.plus(result.time != null ? result.time : Duration.ZERO) : null
            );
        }

        public SheetSeqResult multiply(double factor) {
            var multedCost = cost * factor;
            Duration multedTime = time != null ? Duration.ofMinutes((long) (time.toMinutes() * factor)) : null;

            return new SheetSeqResult(
                    multedCost,
                    multedTime
            );
        }
    }
}
