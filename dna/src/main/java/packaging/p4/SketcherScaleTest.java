package packaging.p4;

import core.BaseSequence;
import datastructures.reference.IDNASketch;
import dnacoders.tree.sketchers.AbstractHashSketcher;
import utils.FuncUtils;
import utils.Pair;
import utils.analyzing.Aggregator;
import utils.csv.BufferedCsvWriter;
import utils.lsh.minhash.MinHashLSH;
import java.util.*;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public class SketcherScaleTest {

    public static void main(String[] args) {
        int n = FuncUtils.tryOrElse(() -> Integer.parseInt(args[0]), () -> 1_000_000);
        var tolFactors = Arrays.asList(0.0f, 0.05f, 0.1f, 0.5f, 1.0f, 1.5f, 2.0f, 3.0f, 4.0f, 5.0f);
        BufferedCsvWriter writer = new BufferedCsvWriter("sketcher.csv");
        if (writer.isEmpty())
            writer.appendNewLine(
                    "sketcher",
                    "flavor",
                    "address size",
                    "lsh type",
                    "lsh k",
                    "lsh r",
                    "trials",
                    "n",
                    "t",
                    "t factor",
                    "sketch size (bytes)",
                    "time (s)",
                    "avg",
                    "min",
                    "max",
                    "std",
                    "aggregate"
            );

        int k = 6;
        int r = 5;
        for (int addrSize : Arrays.asList(80)) {
            for (var factor : tolFactors) {
                for (var trial : Arrays.asList(1, 2, 3, 4, 5, 6)) {
                    for (var flavor : List.of(AbstractHashSketcher.Builder.Flavor.F1)) {
                        var lsh = MinHashLSH.newSeqLSHTraditional(k, r);
                        var sketcher = AbstractHashSketcher.builder()
                                .setAddressSize(addrSize)
                                .setFlavor(flavor)
                                .setLsh(lsh)
                                .build();

                        int tol = (int) (factor * n);
                        List<IDNASketch.HashSketch> sketches = new ArrayList<>(trial);
                        System.out.println("creating sketches for address size: " + addrSize + ", n: " + n + ", t: " + tol + ", trials: " + trial);
                        long start = System.currentTimeMillis();
                        for (int i = 0; i < trial; i++)
                            sketches.add(sketcher.createSketch(n, tol));
                        long end = System.currentTimeMillis();
                        System.out.println("created sketches");

                        int sizeInBytes = sketches.get(0).sizeInBytes();
                        System.out.println("obtaining addresses");
                        List<BaseSequence[]> addresses = sketches.stream().map(IDNASketch::addresses).toList();
                        System.out.println("obtained addresses");


                        System.out.println("computing scores");
                        List<double[]> scores = addresses.stream().map(addrs -> Arrays.stream(addrs).parallel().mapToDouble(addr -> TreeScaleTest.score(addr, 1f, 1f, lsh)).toArray()).toList();


                        int index = max(sum(scores));
                        var scoreAggregate = Aggregator.aggregateNumbers(scores.get(index), true);

                        System.out.println("computing errors");
                        double[] errors = Arrays.stream(addresses.get(index)).parallel().mapToDouble(TreeScaleTest::errorScore).toArray();
                        System.out.println("aggregating errors");
                        Aggregator.NumberAggregates errorAggregate = Aggregator.aggregateNumbers(errors, true);

                        System.out.println("computing dists");
                        double[] dists = Arrays.stream(addresses.get(index)).parallel().mapToDouble(seq -> TreeScaleTest.distScore(seq, lsh)).toArray();

                        System.out.println("aggregating dists");
                        Aggregator.NumberAggregates distAggregate = Aggregator.aggregateNumbers(dists, true);

                        System.out.println("writing to csv");
                        float time = (end - start) / 1000f;
                        writer.appendNewLine(
                                Stream.of(
                                        sketcher.getClass().getSimpleName(),
                                        flavor.name(),
                                        addrSize,
                                        lsh.getClass().getSimpleName(),
                                        k,
                                        r,
                                        trial,
                                        n,
                                        tol,
                                        factor,
                                        sizeInBytes,
                                        time,

                                        errorAggregate.avg(),
                                        errorAggregate.min(),
                                        errorAggregate.max(),
                                        errorAggregate.stdDev(),
                                        "error"
                                ).map(Objects::toString).toArray(String[]::new)
                        );
                        writer.appendNewLine(
                                Stream.of(
                                        sketcher.getClass().getSimpleName(),
                                        flavor.name(),
                                        addrSize,
                                        lsh.getClass().getSimpleName(),
                                        k,
                                        r,
                                        trial,
                                        n,
                                        tol,
                                        factor,
                                        sizeInBytes,
                                        time,

                                        distAggregate.avg(),
                                        distAggregate.min(),
                                        distAggregate.max(),
                                        distAggregate.stdDev(),
                                        "dist"
                                ).map(Objects::toString).toArray(String[]::new)
                        );
                        writer.appendNewLine(
                                Stream.of(
                                        sketcher.getClass().getSimpleName(),
                                        flavor.name(),
                                        addrSize,
                                        lsh.getClass().getSimpleName(),
                                        k,
                                        r,
                                        trial,
                                        n,
                                        tol,
                                        factor,
                                        sizeInBytes,
                                        time,

                                        scoreAggregate.avg(),
                                        scoreAggregate.min(),
                                        scoreAggregate.max(),
                                        scoreAggregate.stdDev(),
                                        "score"
                                ).map(Objects::toString).toArray(String[]::new)
                        );
                    }
                }
            }
        }

        writer.close();
    }

    static double[] sum(List<double[]> values) {
        return values.stream().parallel().mapToDouble(a -> DoubleStream.of(a).parallel().sum()).toArray();
    }

    static int min(double[] values) {
        return FuncUtils.enumerate(DoubleStream.of(values)).min(Comparator.comparing(Pair::getT2)).map(Pair::getT1).orElseThrow();
    }

    static int max(double[] values) {
        return FuncUtils.enumerate(DoubleStream.of(values)).max(Comparator.comparing(Pair::getT2)).map(Pair::getT1).orElseThrow();
    }
}
