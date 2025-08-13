package packaging.btw2023_extended;

import core.BaseSequence;
import core.dnarules.DNARulesCollection;
import core.dnarules.SuperBasicDNARules;
import datastructures.container.Container;
import datastructures.container.impl.SizedDNAContainer;
import datastructures.container.translation.DNAAddrManager;
import dnacoders.dnaconvertors.RotatingQuattro;
import dnacoders.dnaconvertors.RotatingTre;
import utils.Coder;
import utils.FuncUtils;
import utils.Pair;
import utils.analyzing.Aggregator;
import utils.csv.BufferedCsvReader;
import utils.csv.BufferedCsvWriter;
import utils.csv.CsvLine;
import utils.lsh.LSH;
import utils.lsh.minhash.MinHashLSH;
import utils.rq.RQCoder;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class BTW2023_Reference_Extended {

    public static final Coder<String, byte[]> BIJECTIVE_STRING_CODER = new Coder<>() {
        @Override
        public  byte[] encode(String input) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(Short.BYTES * input.length());
            for (int i = 0; i < input.length(); i++)
                byteBuffer.putShort((short) input.charAt(i));

            return byteBuffer.array();
        }

        @Override
        public String decode(byte[] input) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(input);
            StringBuilder result = new StringBuilder();
            while (byteBuffer.hasRemaining())
                result.append((char) byteBuffer.getShort());

            return result.toString();
        }
    };

    static String[] HEADER = new String[] {
            "time in seconds",
            "coder",
            "LSH type",
            "addr_size",
            "addr_permutations",
            "payload_size",
            "payload_padding",
            "payload_permutations",
            "oligo size",
            "num objects inserted",
            "MBs",
            "num oligos",
            "num oligos / object",
            "bit rate",
            "payload bit rate",
            "addr in use",
            "bad addrs",

            "min oligos gc",
            "max oligo gc",
            "avg oligo gc",
            "std oligo gc",

            "min oligo hp",
            "max oligo hp",
            "avg oligo hp",
            "std oligo hp",

            "min Addrs gc",
            "max Addrs gc",
            "avg Addrs gc",
            "std Addrs gc",

            "min Addrs hp",
            "max Addrs hp",
            "avg Addrs hp",
            "std Addrs hp",

            "min Payloads gc",
            "max Payloads gc",
            "avg Payloads gc",
            "std Payloads gc",

            "min Payloads hp",
            "max Payloads hp",
            "avg Payloads hp",
            "std Payloads hp"
    };


    public static void main(String[] args) {
        experiment();
        //perform(FuncUtils.loadConfigFile(args[0]));
    }

    static void experiment() {
        long count = 500;
        int k = 5;
        int r = 5;
        long nBits = -1L;
        String path = "../gfbio.csv";
        //String path = "D:/Data Sets/0165113-230224095556074/0165113-230224095556074.csv";
        String delim = "\t";
        String id = "gbifID";
        DNARulesCollection gcRule = new DNARulesCollection();
        gcRule.addRule(SuperBasicDNARules.INSTANCE.getRules().get("GC Error"));
        DNARulesCollection hpRule = new DNARulesCollection();
        hpRule.addRule(SuperBasicDNARules.INSTANCE.getRules().get("HP Error"));
        Coder<byte[], BaseSequence> rq = new RQCoder(seq -> hpRule.evalErrorProbability(seq) < 0.8f, seq -> gcRule.evalErrorProbability(seq) < 0.8f, false);
        var rqCoder = Coder.fuse(BIJECTIVE_STRING_CODER, rq);
        for (Coder<String, BaseSequence> coder : List.of(RotatingTre.INSTANCE, RotatingQuattro.INSTANCE, rqCoder)) {
            for (int payloadSize : new int[] {120, 140, 160}) {
                for (int addrPermutations : new int[] {0, 2, 4, 8, 16}) {
                    for (int payloadPadding : new int[] {0, 20, 40}) {
                        for (int payloadPermutations : new int[] {0, 2, 4, 8, 16}) {
                            for (int addrSize : new int[] {40, 60, 80}) {
                                for (Supplier<LSH<BaseSequence>> lsh : List.of(
                                        //() -> MinHashLSH.newSeqLSHLight(k, r),
                                        (Supplier<LSH<BaseSequence>>) () -> MinHashLSH.newSeqLSHTraditional(k, r)//,
                                        //() -> MinHashLSH.newSeqLSHBloom(k, r, BloomFilter.numBits(0.01d, count), BloomFilter.numHashFunctions(0.01d))
                                )) {
                                    calc(
                                            path,
                                            delim,
                                            coder,
                                            coder.getClass().getSimpleName(),
                                            count,
                                            addrSize,
                                            addrPermutations,
                                            payloadSize,
                                            payloadPadding,
                                            payloadPermutations,
                                            nBits,
                                            id,
                                            lsh.get(),
                                            lsh.get(),
                                            null
                                    );
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void calc(String csvPath, String delim, Coder<String, BaseSequence> coder, String coderName, long numEntries, int addressSize, int addressPermutations, int payloadSize, int payloadPadding, int payloadPermutations, long nBits, String id, LSH<BaseSequence> oligoLSH, LSH<BaseSequence> lshAddrs, String detailedOutput) {

        DNAAddrManager atm = DNAAddrManager
                .builder()
                .setLsh(lshAddrs)
                .setAddrSize(addressSize)
                .setNumPermutations(addressPermutations)
                .setAddressRoutingContainer(Container.discardingContainer())
                .setAddressTranslationContainer(Container.discardingContainer())
                .build();


        SizedDNAContainer dnaContainer = SizedDNAContainer
                .builder()
                .setAddressManager(atm)
                .setOligoLSH(oligoLSH)
                .setPayloadSize(payloadSize)
                .setNumGcCorrectionsPayload(payloadPadding)
                .setNumPayloadPermutations(payloadPermutations)
                .build();

        Container<Long, String> container = Container.transform(dnaContainer, coder);

        BufferedCsvReader reader = new BufferedCsvReader(csvPath, delim);

        AtomicReference<Long> numBytesPayload = new AtomicReference<>(0L);
        AtomicReference<Integer> numEntriesInserted = new AtomicReference<>(0);
        System.out.println("reading lines...");
        List<CsvLine> lines = reader.stream().limit(numEntries).toList();
        System.out.println("inserting " + numEntries);

        long t1 = System.currentTimeMillis();
        lines.stream().parallel().forEach(line -> {
            var key = Long.parseLong(line.get(id));
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


        BufferedCsvWriter csv = new BufferedCsvWriter("summary.csv", true);
        if (csv.isEmpty())
            csv.appendNewLine(HEADER);

        List<Double> o_gcsList = new ArrayList<>((int) numEntries);
        List<Double> o_hpsList = new ArrayList<>((int) numEntries);

        List<Double> a_gcsList = new ArrayList<>((int) numEntries);
        List<Double> a_hpsList = new ArrayList<>((int) numEntries);

        List<Double> p_gcsList = new ArrayList<>((int) numEntries);
        List<Double> p_hpsList = new ArrayList<>((int) numEntries);

        int allOligosCount = dnaContainer.getSegmentedOligos().stream().parallel().mapToInt(a -> a.length).sum();

        System.out.println("calculating stats..");

        final BufferedCsvWriter detailed;
        if (detailedOutput != null) {
            detailed = new BufferedCsvWriter(detailedOutput, true);
            if (detailed.isEmpty())
                detailed.appendNewLine("path", "coder", "count", "addrSize", "addrPermutations", "payloadSize", "payloadPadding", "payloadPermutations", "lshType", "k", "r", "b", "nBits", "addrGC", "addrHP", "oligoGC", "oligoHP", "payloadGC", "payloadHP");
        }
        else
            detailed = null;

        String lshType = BTW2023_PreTranslate_Extended.parseLSH(oligoLSH);
        int k;
        int r;
        int b;


        if (oligoLSH instanceof MinHashLSH<BaseSequence, ?> minHashLSH) {
            k = minHashLSH.getK();
            r = minHashLSH.getR();
            b = minHashLSH.getB();
        }
        else {
            k = -1;
            r = -1;
            b = -1;
        }

        Lock lock = new ReentrantLock();
        dnaContainer.getSegmentedOligos().stream().parallel().flatMap(Arrays::stream).forEach(ad -> {
            var addr = ad.address();
            var payload = ad.payload();
            var oligo = ad.join();

            double addrGC = addr.gcContent();
            double addrHP = addr.longestHomopolymer();

            double oligoGC = oligo.gcContent();
            double oligoHP = oligo.longestHomopolymer();

            double payloadGC = payload.gcContent();
            double payloadHP = payload.longestHomopolymer();

            lock.lock();
            a_gcsList.add(addrGC);
            a_hpsList.add(addrHP);

            p_gcsList.add(payloadGC);
            p_hpsList.add(payloadHP);

            o_gcsList.add(oligoGC);
            o_hpsList.add(oligoHP);
            lock.unlock();

            if (detailed != null) {
                detailed.appendNewLine(
                        Stream.of(csvPath, coderName, numEntries, addressSize, addressPermutations, payloadSize, payloadPadding, payloadPermutations, lshType, k, r, b, nBits, addrGC, addrHP, oligoGC, oligoHP, payloadGC, payloadHP)
                                .map(Objects::toString)
                                .toArray(String[]::new));
            }
        });

        System.out.println("aggregating...");
        var gcStatesOligos = Aggregator.aggregateNumbers(o_gcsList, true);
        var hpStatesOligos = Aggregator.aggregateNumbers(o_hpsList, true);

        var gcStatesAddrs = Aggregator.aggregateNumbers(a_gcsList, true);
        var hpStatesAddrs = Aggregator.aggregateNumbers(a_hpsList, true);

        var gcStatesPayloads = Aggregator.aggregateNumbers(p_gcsList, true);
        var hpStatesPayloads = Aggregator.aggregateNumbers(p_hpsList, true);

        csv.appendNewLine(
                FuncUtils.enumerate(Stream.of(
                        timeInSecs,
                        coderName,
                        lshType,
                        addressSize,
                        addressPermutations,
                        payloadSize,
                        payloadPadding,
                        payloadPermutations,
                        addressSize + payloadSize,
                        numEntries,
                        numBytesPayload.get() / 1000_000d,
                        allOligosCount,
                        (float) allOligosCount / numEntries,
                        (numBytesPayload.get() * 8L) / (double) ((long) allOligosCount * (addressSize + payloadSize)),
                        (numBytesPayload.get() * 8L) / (double) ((long) allOligosCount * payloadSize),
                        atm.size(),
                        atm.badAddressesCount(),

                        gcStatesOligos.min(),
                        gcStatesOligos.max(),
                        gcStatesOligos.avg(),
                        gcStatesOligos.stdDev(),

                        hpStatesOligos.min(),
                        hpStatesOligos.max(),
                        hpStatesOligos.avg(),
                        hpStatesOligos.stdDev(),

                        gcStatesAddrs.min(),
                        gcStatesAddrs.max(),
                        gcStatesAddrs.avg(),
                        gcStatesAddrs.stdDev(),

                        hpStatesAddrs.min(),
                        hpStatesAddrs.max(),
                        hpStatesAddrs.avg(),
                        hpStatesAddrs.stdDev(),

                        gcStatesPayloads.min(),
                        gcStatesPayloads.max(),
                        gcStatesPayloads.avg(),
                        gcStatesPayloads.stdDev(),

                        hpStatesPayloads.min(),
                        hpStatesPayloads.max(),
                        hpStatesPayloads.avg(),
                        hpStatesPayloads.stdDev()
                ).map(Objects::toString)).peek(vs -> System.out.println(HEADER[vs.getT1()] + ": " + vs.getT2())).map(Pair::getT2).toArray(String[]::new)
        );

        System.out.println("done");

        if (detailed != null)
            detailed.close();
        csv.close();
    }
}
