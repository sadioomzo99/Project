package datastructures.lightweight;

import datastructures.lightweight.index.PSMA;
import datastructures.lightweight.index.ColumnImprint;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class LWITest {
    public static void main(String[] args) {
        Random rand = new Random();
        long min = 400;
        long max = 10000;
        int count = 10;
        List<Long> sortedData = Stream.generate(() -> rand.nextLong(min, max)).limit(count).sorted().distinct().toList();
        List<Double> sortedDataDoubles = sortedData.stream().mapToDouble(__ -> __).boxed().toList();
        ColumnImprint ci = new ColumnImprint(40, sortedDataDoubles, true);
        PSMA psma = new PSMA(sortedData);

        System.out.println("data: " + sortedDataDoubles);

        System.out.println("\nPSMA");
        System.out.println(psma);

        System.out.println("\nCI");
        System.out.println(ci);
        double p = rand.nextDouble(min, max);
        double e = p + 1000d;

        System.out.println("\nrange: [" + p + ", " + e + "]");
        System.out.println("\nCI: " + ci.simpleRangeQuery(p, e));
        System.out.println("\nPSMA range in:   " + psma.findDataRangeAsRange((long) p, (long) e));
        System.out.println("PSMA indexes in: " + psma.findDataRange((long) p, (long) e).boxed().toList());
    }
}
