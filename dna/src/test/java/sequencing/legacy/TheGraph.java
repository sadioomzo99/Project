package sequencing.legacy;

import sequencing.utils.Commons;
import utils.csv.BufferedCsvWriter;
import utils.sequencing.SimpleSequencingPlatform;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public class TheGraph {

    public static void main(String[] args) {

        BufferedCsvWriter writer = new BufferedCsvWriter("costs_test.csv", false);
        writer.appendNewLine(
                "nodeSize in nts",
                "platform",
                "generation",
                "total_nts",
                "factor",
                "costs"
        );


        List<SimpleSequencingPlatform> plats = List.of(
                Commons.SANGER,
                Commons.NGS_GOV,
                Commons.NGS_PAPER,
                Commons.ONT,
                Commons.PAC_BIO
        );

        List<SimpleSequencingPlatform> platsWithScaled = new ArrayList<>(plats);
        platsWithScaled.addAll(DoubleStream.of(0.2, 0.4, 0.6, 0.8).mapToObj(s -> plats.stream().map(p -> p.scale(s))).flatMap(__ -> __).toList());

            for (long nodeSizeNts = 500L; nodeSizeNts < 1_000_000L; nodeSizeNts += 5000L) {
                for (SimpleSequencingPlatform plat : platsWithScaled) {
                    double totalGigaNts = totalGigaNts(plat, nodeSizeNts / 1000_000_000d);
                    double cost = plat.sequence(totalGigaNts).average().doubleValue();
                    String[] factors = plat.name().split("_");
                    String factor = factors.length > 1 ? factors[1] : "0";
                    write(
                            writer,
                            nodeSizeNts,
                            plat,
                            totalGigaNts,
                            factor,
                            cost
                    );
                }
            }

        writer.close();
    }

    public static double totalGigaNts(SimpleSequencingPlatform plat, double gigaNts) {
        return switch (plat.generation()) {
            case FIRST -> Commons.totalGigaNts(gigaNts, Commons.CONFIGS.get(Commons.SANGER),2.0d, 99.9d, 99.9d);
            case SECOND -> Commons.totalGigaNts(gigaNts, Commons.CONFIGS.get(Commons.NGS),2.0d, 99.9d, 99.9d);
            case THIRD -> Commons.totalGigaNts(gigaNts, Commons.CONFIGS.get(Commons.ONT),2.0d, 99.9d, 92.5d);
            case PAC_BIO -> Commons.totalGigaNts(gigaNts, Commons.CONFIGS.get(Commons.PAC_BIO),2.0d, 99.9d, 99.5d);
            case OTHER -> throw new RuntimeException("not implemented");
        };
    }

    public static void write(
            BufferedCsvWriter writer,
            long nodeSizeNts,
            SimpleSequencingPlatform platform,
            double totalNts,
            String factor,
            double cost
    ) {
        writer.appendNewLine(
                Stream.of(
                        nodeSizeNts,
                        platform.name(),
                        platform.generation().name(),
                        totalNts,
                        factor,
                        cost
                ).map(Objects::toString).toArray(String[]::new)
        );
    }
}
