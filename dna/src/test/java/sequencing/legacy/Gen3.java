package sequencing.legacy;

import sequencing.utils.Commons;
import utils.sequencing.Generation;
import utils.sequencing.SimpleSequencingPlatform;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Gen3 {

    public static void main(String[] args) {
        int oligoSize = 150;
        int addrSize = 80;
        int b = 20;
        int c = 20;
        int numberTuples = 1000_000;
        int leafSize = 2 * c - 1;
        int internalNodePointers = 2 * b - 1;
        int tupleSize = 24;
        int pointerSize = 8;
        long numLeafNodes = (int) Math.ceil((double) numberTuples / leafSize);
        long numInternalNodes = numLeafNodes / (b - 1);
        int payloadSize = oligoSize - addrSize;
        var config = new Commons.StrandConfig(addrSize, oligoSize - payloadSize);
        double bitrate = 2d;
        double pointersBitrate = 2d;
        long numPointers = numInternalNodes > 0 ?
                numInternalNodes * internalNodePointers // the number of children pointers
                        + numLeafNodes / b - 1L : 0L; // the number of pointers to the nodes to the right (above leaves' level)

        double objectsAsGigaNucleotidesRaw = (double) numberTuples * tupleSize * 4d / 1000_000_000d;
        double pointersAsGigaNucleotidesRaw = (double) numPointers * pointerSize * 4d / 1000_000_000d;

        List<Result> results = new ArrayList<>(16);
        for (SimpleSequencingPlatform plat1 : Commons.SIMPLE_PLATFORMS) {
            for (SimpleSequencingPlatform plat2 : Commons.SIMPLE_PLATFORMS) {
                double pointersGigaNucleotides =  Commons.totalGigaNts(pointersAsGigaNucleotidesRaw, config, pointersBitrate, 99.99d, plat1.readAccuracy().average().doubleValue());
                double objectsGigaNucleotides = Commons.totalGigaNts(objectsAsGigaNucleotidesRaw, config, bitrate, 99.99d, plat2.readAccuracy().average().doubleValue());
                double costInternals = Commons.totalGigaNts(pointersGigaNucleotides, config, pointersBitrate) * plat1.costPerGigaBase().average().doubleValue();
                double costLeaves = Commons.totalGigaNts(objectsGigaNucleotides, config, bitrate) * plat2.costPerGigaBase().average().doubleValue();

                results.add(
                        new Result(
                                plat1.generation(),
                                plat2.generation(),
                                pointersGigaNucleotides,
                                objectsGigaNucleotides,
                                costInternals,
                                costLeaves
                        )
                );
            }
        }

        results.stream().sorted(Comparator.comparingDouble(Result::totalCosts)).forEach(System.out::println);
    }

    public record Result(Generation internals, Generation leaves, double pointersGigaNts, double objectsGigaNts, double internalCosts, double leafCosts) {
        public double totalCosts() {
            return  internalCosts + leafCosts;
        }

        public double totalGigaNts() {
            return  pointersGigaNts + objectsGigaNts;
        }

    }
}
