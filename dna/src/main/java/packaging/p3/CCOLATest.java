package packaging.p3;

import core.BaseSequence;
import core.dnarules.BasicDNARules;
import core.dnarules.SuperBasicDNARules;
import datastructures.KVEntry;
import datastructures.cola.COLA;
import datastructures.cola.PartitionedCOLA;
import datastructures.container.impl.SizedDNAContainer;
import datastructures.container.translation.DNAAddrManager;
import dnacoders.dnaconvertors.RotatingTre;
import utils.AddressedDNA;
import datastructures.cola.run.managers.BasicRunsManager;
import datastructures.cola.runbuilderstrategy.RunBuilderStrategy;
import packaging.p2.Experiment1;
import utils.*;
import utils.csv.BufferedCsvReader;
import utils.csv.CsvLine;
import utils.lsh.LSH;
import utils.lsh.minhash.MinHashLSH;
import utils.lsh.storage.LSHStorage;
import utils.rq.RQCoder;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CCOLATest {
    public static void main(String[] args) {
        String[] mappings = new String[] {"time", "icao24", "lon", "lat", "velocity", "baroaltitude"};
        Map<String, Integer> colMappings = IntStream.range(0, mappings.length).boxed().collect(Collectors.toMap(i -> mappings[i], i -> i));
        var mappingsTypes = new Packer.Type[] {Packer.Type.INT, Packer.Type.SHORT, Packer.Type.DOUBLE, Packer.Type.DOUBLE, Packer.Type.DOUBLE, Packer.Type.DOUBLE};

        Coder<CsvLine, byte[]> recordExtractor = CSVLinePacker.fromTypeByAttributeNameArrays(mappings, mappingsTypes);

        Coder<byte[], BaseSequence> byteCoder1 = new RQCoder(
                packet -> SuperBasicDNARules.INSTANCE.evalErrorProbability(packet) <= 0.6f,
                strand -> SuperBasicDNARules.INSTANCE.evalErrorProbability(strand) <= 0.5f);

        Coder<byte[], BaseSequence> byteCoder2 = Coder.of(
                bs -> SeqBitStringConverter.transform(new BitString(bs)),
                seq -> SeqBitStringConverter.transform(seq).toBytes()
        );


        var encoder = Base64.getEncoder();
        var decoder = Base64.getDecoder();
        Coder<byte[], BaseSequence> byteCoder3 = Coder.fuse(
                Coder.of(encoder::encodeToString, decoder::decode),
                RotatingTre.INSTANCE
        );

        Coder<CsvLine, BaseSequence> valueCoder = Coder.fuse(recordExtractor, byteCoder3);

        Coder<KVEntry<Long, CsvLine>, BaseSequence> entryMapper = Coder.of(
                e -> BaseSequence.join(
                        DNAPacker.pack(e.key(), DNAPacker.LengthBase.INT_64),
                        valueCoder.encode(e.value())
                ),
                seq -> new KVEntry<>(
                        DNAPacker.LengthBase.INT_64.unpackSingle(seq, false).longValue(),
                        valueCoder.decode(seq.window(DNAPacker.LengthBase.INT_64.totalSize()))
                )
        );

        int addressSize = 80;
        int payloadSize = 120;
        DNAAddrManager atm = DNAAddrManager
                .builder()
                .setLsh(MinHashLSH.newSeqAmpLSHTraditional(5, 200, 20, LSHStorage.AmplifiedLSHStorage.Amplification.OR))
                .setAddrSize(addressSize)
                .build();

        SizedDNAContainer container = SizedDNAContainer
                .builder()
                .setAddressManager(atm)
                .setPayloadSize(payloadSize)
                .build();



        /*
        int numEntries = 10;
        BufferedCsvReader reader = new BufferedCsvReader("D:/Data Sets/A321_valid_100_full.csv");
        int partitionStart = 0;


         */

        BufferedCsvReader reader = new BufferedCsvReader(args[0]);
        int numEntries = Integer.parseInt(args[1]);
        int partitionStart = Integer.parseInt(args[2]);


        System.out.println("inserting " + numEntries + " into cola...");


        //COLA<Long, CsvLine> cola = new PartitionedCOLA<>(new BasicRunsManager<>(RunBuilderStrategy.PartitionedCAODNA(true, partitionStart, container, entryMapper)), partitionStart);
        COLA<Long, CsvLine> cola = new PartitionedCOLA<>(new BasicRunsManager<>(RunBuilderStrategy.partitionedDNA_PSMA(true, partitionStart, container, entryMapper)), partitionStart);
        //COLA<Long, CsvLine> cola = new PartitionedCOLA<>(new BasicRunsManager<>(RunBuilderStrategy.partitionedDNA_Header(true, partitionStart, container, entryMapper, HCOLAHeaders.PSMA_COLA_HEADER_CODER, HCOLAHeaders.generatorPSMA(true))), partitionStart);
        //COLA<Long, CsvLine> cola = new PartitionedCOLA<>(new BasicRunsManager<>(RunBuilderStrategy.partitionedDNA_Header(true, partitionStart, container, entryMapper, HCOLAHeaders.CI_COLA_HEADER_CODER, HCOLAHeaders.generatorCI(40))), partitionStart);


        AtomicInteger numBytesPayload = new AtomicInteger(0);
        AtomicInteger numBytesPayloadDNA = new AtomicInteger(0);
        AtomicInteger numEntriesInserted = new AtomicInteger(0);
        int numEntriesDNAStart = 1 << (partitionStart + 1) - 1;
        reader.stream().limit(numEntries).forEach(line -> {
            System.out.println("inserting #" + numEntriesInserted.addAndGet(1) + " entry into COLA");
            var key = Long.parseLong(line.get("Id"));
            var m = line.get(mappings);
            var readBytes = Integer.BYTES + Arrays.stream(m).mapToInt(String::length).sum();
            numBytesPayload.addAndGet(readBytes);
            if (numEntriesInserted.get() > numEntriesDNAStart)
                numBytesPayloadDNA.addAndGet(readBytes);
            cola.insert(
                    key,
                    new CsvLine(m, colMappings));

        });

        reader.close();
        System.out.println("read data: " + (numBytesPayload.get() / 1000_000d) + " MBs");
        System.out.println(cola);
        System.out.println("evaluating results...\n");


        System.out.println("cola size: " + cola.size());
        Collection<AddressedDNA[]> oligosSegmented = container.getSegmentedOligos();
        List<BaseSequence> allOligos = oligosSegmented.stream().flatMap(Arrays::stream).map(AddressedDNA::join).toList();
        List<BaseSequence> addresses = oligosSegmented.stream().flatMap(Arrays::stream).map(AddressedDNA::address).toList();
        System.out.println("num objects on DNA: " + (numEntries - numEntriesDNAStart + 1));
        System.out.println("num oligos: " + allOligos.size());
        System.out.println("average object segmentation size: " + (float) allOligos.size() / (numEntries - numEntriesDNAStart + 1));
        System.out.println("bit rate: " + (numBytesPayloadDNA.get() * 8) / (double) (allOligos.size() * (addressSize + payloadSize)) + " b/nt");


        LSH<BaseSequence> addrLSH = atm.getLsh();

        System.out.println("\ndist:");
        LSH<BaseSequence> lsh = MinHashLSH.newSeqAmpLSHTraditional(5, 200, 20, LSHStorage.AmplifiedLSHStorage.Amplification.OR);
        lsh.insertParallel(allOligos);
        System.out.println("seqs: " + Experiment1.distStats(allOligos, lsh));
        System.out.println("addresses: " + Experiment1.distStats(addresses, addrLSH));

        System.out.println("\nerror:");
        System.out.println("oligos: " + allOligos.stream().parallel().mapToDouble(BasicDNARules.INSTANCE::evalErrorProbability).summaryStatistics());
    }
}
