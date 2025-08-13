package packaging.btw2023_extended;

import core.BaseSequence;
import datastructures.container.Container;
import datastructures.container.translation.DNAAddrManager;
import datastructures.hashtable.BloomFilter;
import utils.csv.BufferedCsvWriter;
import utils.lsh.LSH;
import utils.lsh.minhash.MinHashLSH;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class BTW2023_PreTranslate_Extended {

    public static void main(String... args) {
        long count = 10_000_000L;

        BufferedCsvWriter csv = new BufferedCsvWriter("summary_t_test.csv", true);
        if (csv.isEmpty())
            csv.appendNewLine("time in secs", "address size", "run size", "k", "lsh", "fpp", "numBits", "numHashFunctions", "optNumHashFuncs", "size in bytes", "count", "num permutations", "total bad addresses", "total bad %", "bad address in run", "bad address in run %", "fail probability", "duration in secs");

        int numPerms = 4;
        int r = 5;
        double eps = 0.01d;
        long numBitsOpt = BloomFilter.numBits(eps, count);
        for (int k : new int[] {6, 7, 8}) {
            for (int addrSize : Arrays.asList(20, 40, 60, 80)) {
                for (LSH<BaseSequence> lsh : Arrays.asList(MinHashLSH.newSeqLSHTraditional(k, r), MinHashLSH.newSeqLSHLight(k, r)))
                    perform(k, addrSize, lsh, numPerms, count, csv, Double.NaN, -1L, -1L);

                for (long numBits : new long[] {numBitsOpt}) {
                    long optNumHashFunctions = BloomFilter.numHashFunctions(numBits, count);
                    for (MinHashLSH.Bloom<BaseSequence> lsh : Arrays.asList(MinHashLSH.newSeqLSHBloom(k, r, numBits, optNumHashFunctions), MinHashLSH.newSeqLSHBloom(k, r, numBits, 1)))
                        perform(k, addrSize, lsh, numPerms, count, csv, BloomFilter.falsePositiveProb(lsh.getNumHashFunctions(), numBits, count), lsh.getNumBits(), lsh.getNumHashFunctions());
                }
            }
        }

        csv.close();
    }

    private static void perform(int k, int addrSize, LSH<BaseSequence> lsh, int numPerms, long count, BufferedCsvWriter csv, double fpp, long numBits, long numHashFunctions) {
        System.out.println("addrSize: " + addrSize + ", k: " + k + ", lsh: " + parseLSH(lsh));
        var atm = DNAAddrManager.builder()
                .setAddressRoutingContainer(Container.discardingContainer())
                .setAddressTranslationContainer(Container.discardingContainer())
                .setAddrSize(addrSize)
                .setLsh(lsh)
                .setNumPermutations(numPerms)
                .build();

        long t1 = System.currentTimeMillis();

        long runSize = 10_000L;
        AtomicLong lastTimeStamp = new AtomicLong(System.currentTimeMillis());
        AtomicLong sortedId = new AtomicLong(1L);
        AtomicLong sumBads_1 = new AtomicLong(0L);
        LongStream.range(0L, count)
                .parallel()
                .forEach(_id -> {
                    long id = sortedId.getAndIncrement();
                    long currentBads = atm.badAddressesCount();
                    atm.routeAndTranslate(_id);
                    if (id % runSize == 0) {
                        long currentTimeStamp = System.currentTimeMillis();
                        float duration = (currentTimeStamp - lastTimeStamp.getAndSet(currentTimeStamp)) / 1000f;
                        System.out.println("count: " + id);
                        long badAddressesInRun = currentBads - sumBads_1.getAndSet(currentBads);
                        double numGeneratedInRun = badAddressesInRun + runSize;
                        csv.appendNewLine(
                                Stream.of(
                                        (currentTimeStamp - t1) / 1000d,
                                        addrSize,
                                        runSize,
                                        k,
                                        parseLSH(lsh),
                                        fpp,
                                        numBits > 0L ? numBits : "NAN",
                                        numHashFunctions > 0L ? numHashFunctions : "NAN",
                                        numHashFunctions == 1L ? "not opt" : "opt",
                                        sizeLSH(lsh),
                                        id,
                                        numPerms,
                                        atm.badAddressesCount(),
                                        ((double) currentBads / count) * 100d,
                                        badAddressesInRun,
                                        (double) badAddressesInRun / runSize,
                                        1d - (runSize / numGeneratedInRun),
                                        duration
                                ).map(Objects::toString).toArray(String[]::new));
                    }
                });

        System.out.println("run done\n------------------");
    }

    public static String parseLSH(LSH<BaseSequence> lsh) {
        if (lsh instanceof MinHashLSH.Bloom<BaseSequence>)
            return "Bloom";
        if (lsh instanceof MinHashLSH.Traditional<BaseSequence>)
            return "Traditional";
        if (lsh instanceof MinHashLSH.Light<BaseSequence>)
            return "Light";

        return lsh.getClass().getSimpleName();
    }

    static long sizeLSH(LSH<BaseSequence> lsh) {
        if (lsh instanceof MinHashLSH.Bloom<BaseSequence> bloom) {
            return 3 * Integer.BYTES + bloom.getNumHashFunctions() * 2L * Long.BYTES + (long) Math.ceil((double) bloom.getNumBits() / Byte.SIZE);
        }

        if (lsh instanceof MinHashLSH.Traditional<BaseSequence> trad)
            return 3 * Integer.BYTES + trad.getStorage().bands().stream()
                    .flatMapToLong(band -> band.getMappings()
                            .values()
                            .stream()
                            .mapToLong(seqs -> Long.BYTES + seqs.stream().mapToLong(seq -> (long) Math.ceil((double) seq.length() / 4L)).sum()))
                    .sum();
        if (lsh instanceof MinHashLSH.Light<BaseSequence> light)
            return 3 * Integer.BYTES + light.getStorage().bands().stream().mapToLong(band -> (long) band.hashSet().size() * Long.BYTES).sum();

        return -1L;
    }
}
