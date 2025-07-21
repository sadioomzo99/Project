package packaging.btw2023;

import core.BaseSequence;
import datastructures.container.Container;
import datastructures.container.impl.SizedDNAContainer;
import datastructures.container.translation.DNAAddrManager;
import utils.AddressedDNA;
import dnacoders.DistanceCoder;
import dnacoders.dnaconvertors.RotatingQuattro;
import utils.csv.BufferedCsvReader;
import utils.csv.BufferedCsvWriter;
import utils.csv.CsvLine;
import utils.lsh.LSH;
import utils.lsh.minhash.MinHashLSH;
import utils.lsh.storage.LSHStorage;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.*;

public class BTW2023_Reference {

    static int[] addrSizes = {60, 80};
    static int[] payloadSizes = {100, 120, 140};

    public static void main(String[] args) {
        for (int addrSize : addrSizes) {
            for (int payloadSize : payloadSizes) {
                System.out.println("addressSize: " + addrSize);
                System.out.println("payloadSize: " + payloadSize);
                calc(args[0], Integer.parseInt(args[1]), addrSize, payloadSize);
            }
        }
    }

    public static void calc(String csvPath, int numEntries, int addressSize, int payloadSize) {

        // 100_000 = 6MB
        // 1_000_000 = 60MB
        DNAAddrManager atm = DNAAddrManager
                .builder()
                .setLsh(MinHashLSH.newSeqAmpLSHTraditional(5, 200, 20, LSHStorage.AmplifiedLSHStorage.Amplification.OR))
                .setAddrSize(addressSize)
                //.setAddressRoutingContainer(Container.discardingContainer())
                //.setAddressTranslationContainer(Container.discardingContainer())
                .build();

        SizedDNAContainer dnaContainer = SizedDNAContainer
                .builder()
                .setAddressManager(atm)
                .setPayloadSize(payloadSize)
                .build();

        Container<Long, String> container = Container.transform(dnaContainer, RotatingQuattro.INSTANCE);

        BufferedCsvReader reader = new BufferedCsvReader(csvPath);

        AtomicReference<Long> numBytesPayload = new AtomicReference<>(0L);
        AtomicReference<Integer> numEntriesInserted = new AtomicReference<>(0);
        System.out.println("reading lines...");
        List<CsvLine> lines = reader.stream().limit(numEntries).toList();
        System.out.println("inserting " + numEntries);

        long t1 = System.currentTimeMillis();
        lines.stream().parallel().forEach(line -> {
            var key = Long.parseLong(line.get("Id"));
            numBytesPayload.updateAndGet(v -> v + line.getLine().length());
            container.put(
                    key,
                    line.getLine());
            System.out.println("inserted " + numEntriesInserted.updateAndGet(v -> v + 1) + " entries into the container");
        });
        double timeInSecs = (System.currentTimeMillis() - t1) / 1000d;

        System.out.println("\ninsertion took: " + timeInSecs + " seconds -> " + numEntries / timeInSecs + " entry/sec");
        reader.close();
        System.out.println("read data: " + (numBytesPayload.get() / 1000_000d) + " MBs");


        BufferedCsvWriter csv = new BufferedCsvWriter("btw.csv", true);
        if (csv.isEmpty()) {
            csv.appendNewLine(
                    "addr_size",
                    "payload_size",
                    "oligo size",
                    "num objects inserted",
                    "dna objects",
                    "MBs",
                    "num oligos",
                    "num oligos / object",
                    "bit rate",
                    "payload bit rate",
                    "addr in use",
                    "bad addrs",

                    "min oligos dist",
                    "max oligos dist",
                    "avg oligos dist",
                    "min oligos gc",
                    "max oligo gc",
                    "avg oligo gc",
                    "min oligo hp",
                    "max oligo hp",
                    "avg oligo hp",

                    "min Addrs dist",
                    "max Addrs dist",
                    "avg Addrs dist",
                    "min Addrs gc",
                    "max Addrs gc",
                    "avg Addrs gc",
                    "min Addrs hp",
                    "max Addrs hp",
                    "avg Addrs hp"
            );
        }

        BufferedCsvWriter states = new BufferedCsvWriter("btw_states.csv", true);
        if (states.isEmpty())
            states.appendNewLine("address size", "payload size", "o_gc", "o_hp", "o_dist", "a_gc", "a_hp", "a_dist");


        DoubleStream.Builder o_gcs = DoubleStream.builder();
        DoubleStream.Builder o_hps = DoubleStream.builder();
        DoubleStream.Builder o_dists = DoubleStream.builder();

        DoubleStream.Builder a_gcs = DoubleStream.builder();
        DoubleStream.Builder a_hps = DoubleStream.builder();
        DoubleStream.Builder a_dists = DoubleStream.builder();

        Lock sbLock = new ReentrantLock();

        LSH<BaseSequence> addrLSH = MinHashLSH.newSeqAmpLSHTraditional(5, 200, 20, LSHStorage.AmplifiedLSHStorage.Amplification.OR);
        LSH<BaseSequence> oligoLSH = MinHashLSH.newSeqAmpLSHTraditional(5, 200, 20, LSHStorage.AmplifiedLSHStorage.Amplification.OR);
        System.out.println("getting all addressed DNAs from the DNA container...");
        var segmentedOligos = dnaContainer.getSegmentedOligos();

        System.out.println("flatmapping them...");
        var allOligos = segmentedOligos.stream().parallel().flatMap(Arrays::stream).map(AddressedDNA::join).toList();

        System.out.println("extracting all addresses from the oligo list...");
        var allAddresses = segmentedOligos.stream().parallel().flatMap(Arrays::stream).map(AddressedDNA::address).toList();

        System.out.println("inserting all " + allOligos.size() + " oligos into an LSH...");
        oligoLSH.insertParallel(allOligos);
        System.out.println("inserting all " + allAddresses.size() + " addresses into an LSH...");
        addrLSH.insertParallel(allAddresses);

        System.out.println("calculating stats..");
        IntStream.range(0, allOligos.size()).parallel().mapToObj(i -> {
            var oligo = allOligos.get(i);
            var addr = allAddresses.get(i);

            var o_gc = oligo.gcContent();
            var o_hp = oligo.longestHomopolymer();
            var o_dist = Math.min(DistanceCoder.distanceScoreExclusive(oligo, oligoLSH), DistanceCoder.distanceScore(oligo.complement(), oligoLSH));

            var a_gc = addr.gcContent();
            var a_hp = addr.longestHomopolymer();
            var a_dist = Math.min(DistanceCoder.distanceScoreExclusive(addr, addrLSH), DistanceCoder.distanceScore(addr.complement(), addrLSH));

            sbLock.lock();
            o_gcs.add(o_gc);
            o_hps.add(o_hp);
            o_dists.add(o_dist);

            a_gcs.add(a_gc);
            a_hps.add(a_hp);
            a_dists.add(a_dist);
            sbLock.unlock();

            return Stream.of(
                    addressSize,
                    payloadSize,
                    o_gc,
                    o_hp,
                    o_dist,
                    a_gc,
                    a_hp,
                    a_dist
            ).map(Objects::toString).toArray(String[]::new);
        }).forEachOrdered(states::appendNewLine);

        states.close();

        System.out.println("aggregating...");
        var distStatesOligos = o_dists.build().parallel().summaryStatistics();
        var gcStatesOligos = o_gcs.build().parallel().summaryStatistics();
        var hpStatesOligos = o_hps.build().parallel().summaryStatistics();

        var distStatesAddrs = a_dists.build().parallel().summaryStatistics();
        var gcStatesAddrs = a_gcs.build().parallel().summaryStatistics();
        var hpStatesAddrs = a_hps.build().parallel().summaryStatistics();

        csv.appendNewLine(
                Stream.of(
                        addressSize,
                        payloadSize,
                        addressSize + payloadSize,
                        numEntries,
                        dnaContainer.size(),
                        numBytesPayload.get() / 1000_000d,
                        allOligos.size(),
                        (float) allOligos.size() / numEntries,
                        (numBytesPayload.get() * 8L) / (double) ((long) allOligos.size() * (addressSize + payloadSize)),
                        (numBytesPayload.get() * 8L) / (double) ((long) allOligos.size() * payloadSize),
                        atm.size(),
                        atm.badAddressesCount(),

                        distStatesOligos.getMin(),
                        distStatesOligos.getMax(),
                        distStatesOligos.getAverage(),
                        gcStatesOligos.getMin(),
                        gcStatesOligos.getMax(),
                        gcStatesOligos.getAverage(),
                        hpStatesOligos.getMin(),
                        hpStatesOligos.getMax(),
                        hpStatesOligos.getAverage(),

                        distStatesAddrs.getMin(),
                        distStatesAddrs.getMax(),
                        distStatesAddrs.getAverage(),
                        gcStatesAddrs.getMin(),
                        gcStatesAddrs.getMax(),
                        gcStatesAddrs.getAverage(),
                        hpStatesAddrs.getMin(),
                        hpStatesAddrs.getMax(),
                        hpStatesAddrs.getAverage()
                ).map(Objects::toString).toArray(String[]::new)
        );

        System.out.println("done");
        csv.close();
    }
}
