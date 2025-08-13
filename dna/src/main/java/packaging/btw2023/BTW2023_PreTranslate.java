package packaging.btw2023;

import datastructures.container.Container;
import datastructures.container.translation.DNAAddrManager;
import utils.FuncUtils;
import utils.csv.BufferedCsvWriter;
import utils.lsh.minhash.MinHashLSH;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class BTW2023_PreTranslate {

    public static void main(String[] args) {
        long count = Long.parseLong(args[0]);
        System.out.println("creating list with " + count + " ids..");
        var atm = DNAAddrManager.builder()
                .setAddrSize(80)
                .setAddressRoutingContainer(Container.discardingContainer())
                .setAddressTranslationContainer(Container.discardingContainer())
                .setLsh(MinHashLSH.newSeqLSHLight(6, 5))
                .build();

        BufferedCsvWriter csv = new BufferedCsvWriter("translation.csv");
        if (csv.isEmpty())
            csv.appendNewLine("count", "bad addresses", "bad percent", "time in sec");

        System.out.println("translating " + count + " ids..");
        long t1 = System.currentTimeMillis();

        AtomicLong counter = new AtomicLong(0L);
        LongStream.range(0L, count).parallel().forEach(id -> {
            atm.routeAndTranslate(id);
            var c = counter.incrementAndGet();
            var mod = c % 10_000;
            if (mod == 0)
                System.out.println("translated " + c + " ids");
            if (c % 20_000 == 0) {
                csv.appendNewLine(
                        Stream.of(
                                count,
                                atm.badAddressesCount(),
                                ((double) atm.badAddressesCount() / count) * 100d,
                                (System.currentTimeMillis() - t1) / 1000d
                        ).map(Objects::toString).toArray(String[]::new)
                );
                System.out.println("------> bad addresses: " + atm.badAddressesCount());
                System.out.println("------> JVM mem usage: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1073741824.0f);
            }
        });


        var millis = System.currentTimeMillis() - t1;
        var seconds = millis / 1000f;
        System.out.println("\n----------------------\ntime: " + FuncUtils.asPrettyString(Duration.ofMillis(millis)) + " -> " + (count / seconds) + " ids/sec");

        if (counter.get() % 10_000 != 0) {
            csv.appendNewLine(
                    Stream.of(
                            count,
                            atm.badAddressesCount(),
                            ((double) atm.badAddressesCount() / count) * 100d,
                            seconds
                    ).map(Objects::toString).toArray(String[]::new)
            );
        }

        csv.close();
    }
}
