package packaging.p3;

import core.BaseSequence;
import core.dnarules.BasicDNARules;
import core.dnarules.SuperBasicDNARules;
import datastructures.KVEntry;
import datastructures.container.impl.SizedDNAContainer;
import datastructures.container.translation.DNAAddrManager;
import utils.AddressedDNA;
import datastructures.container.utils.CBinSearchTree;
import datastructures.searchtrees.BinarySearchTree;
import dnacoders.dnaconvertors.RotatingQuattro;
import packaging.p2.Experiment1;
import utils.*;
import utils.csv.BufferedCsvReader;
import utils.csv.CsvLine;
import utils.lsh.minhash.MinHashLSH;
import utils.lsh.storage.LSHStorage;
import utils.rq.RQCoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CBinTreeTest {

    public static void main(String[] args) {


        String[] mappings = new String[] {"time", "icao24", "lon", "lat", "velocity", "baroaltitude"};
        Map<String, Integer> colMappings = IntStream.range(0, mappings.length).boxed().collect(Collectors.toMap(i -> mappings[i], i -> i));
        var mappingsTypes = new Packer.Type[] {Packer.Type.INT, Packer.Type.SHORT, Packer.Type.DOUBLE, Packer.Type.DOUBLE, Packer.Type.DOUBLE, Packer.Type.DOUBLE};



        var recordExtractor = CSVLinePacker.fromTypeByAttributeNameArrays(mappings, mappingsTypes);

        Coder<byte[], BaseSequence> byteCoder1 = new RQCoder(
                packet -> SuperBasicDNARules.INSTANCE.evalErrorProbability(packet) <= 0.6f,
                strand -> SuperBasicDNARules.INSTANCE.evalErrorProbability(strand) <= 0.5f);

        Coder<byte[], BaseSequence> byteCoder2 = Coder.of(
                bs -> SeqBitStringConverter.transform(new BitString(bs)),
                seq -> SeqBitStringConverter.transformToString(seq).getBytes()
        );

        Coder<byte[], BaseSequence> byteCoder3 = Coder.of(
                bs -> RotatingQuattro.INSTANCE.encode(new String(bs)),
                seq -> RotatingQuattro.INSTANCE.decode(seq).getBytes()
        );

        Coder<CsvLine, BaseSequence> valueCoder = Coder.fuse(recordExtractor, byteCoder2);

        Coder<KVEntry<Integer, CsvLine>, BaseSequence> entryMapper = Coder.of(
                e -> BaseSequence.join(DNAPacker.pack(e.key()), valueCoder.encode(e.value())),
                seq -> new KVEntry<>(DNAPacker.unpack(seq, false).intValue(), valueCoder.decode(seq.window(DNAPacker.LengthBase.INT_32.totalSize())))
        );


        BinarySearchTree<KVEntry<Integer, CsvLine>> binTree = new BinarySearchTree<>();

        //BufferedCsvReader reader = new BufferedCsvReader("D:/Data Sets/A321_valid_1000k_full.csv");
        BufferedCsvReader reader = new BufferedCsvReader(args[0]);
        int numEntries = Integer.parseInt(args[1]);
        //int numEntries = 10_000;


        System.out.println("inserting " + numEntries + " into binary search tree...");
        AtomicReference<Integer> numBytesPayload = new AtomicReference<>(0);
        binTree.insertParallel(
                reader.stream().limit(numEntries).map(line -> {
                    var key = Integer.parseInt(line.get("Id"));
                    var m = line.get(mappings);
                    numBytesPayload.updateAndGet(v -> Integer.BYTES + v + Arrays.stream(m).mapToInt(String::length).sum());
                    return new KVEntry<>(key, new CsvLine(m, colMappings));
                })
        );

        reader.close();
        System.out.println("tree is ready!");


        System.out.println("read data: " + (numBytesPayload.get() / 1000_1000d) + " MBs");
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


        CBinSearchTree.putBinaryTree(container, binTree, entryMapper);
        Collection<AddressedDNA[]> oligos = container.getSegmentedOligos();
        Collection<AddressedDNA> oligosFlat = oligos.stream().flatMap(Arrays::stream).toList();

        System.out.println("num sequences: " + oligosFlat.size());
        System.out.println("average object segmentation size: " + oligos.stream().mapToInt(a -> a.length).summaryStatistics());
        System.out.println("bit rate: " + (numBytesPayload.get() * 8) / (double) (oligosFlat.size() * (addressSize + payloadSize)) + " b/nt");
        System.out.println("bit rate without addresses: " + (numBytesPayload.get() * 8) / (double) (oligosFlat.size() * payloadSize) + " b/nt");

        System.out.println("\ndist:");
        System.out.println("seqs: " + Experiment1.distStats(oligosFlat.stream().parallel().map(AddressedDNA::join).toList(), container.getOligoLSH()));
        System.out.println("addresses: " + Experiment1.distStats(oligosFlat.stream().parallel().map(AddressedDNA::address).toList(), container.getOligoLSH()));
        System.out.println("payloads: " + Experiment1.distStats(oligosFlat.stream().parallel().map(AddressedDNA::payload).toList(), container.getOligoLSH()));

        System.out.println("\nerror:");
        System.out.println("seqs: " + oligosFlat.stream().parallel().map(AddressedDNA::join).mapToDouble(BasicDNARules.INSTANCE::evalErrorProbability).summaryStatistics());
        System.out.println("addresses: " + oligosFlat.stream().parallel().map(AddressedDNA::address).mapToDouble(BasicDNARules.INSTANCE::evalErrorProbability).summaryStatistics());
        System.out.println("payloads: " + oligosFlat.stream().parallel().map(AddressedDNA::payload).mapToDouble(BasicDNARules.INSTANCE::evalErrorProbability).summaryStatistics());
    }
}
