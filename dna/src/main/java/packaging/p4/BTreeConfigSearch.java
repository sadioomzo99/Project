package packaging.p4;

import core.BaseSequence;
import datastructures.KVEntry;
import datastructures.reference.IDNASketch;
import datastructures.searchtrees.BPlusTree;
import dnacoders.dnaconvertors.RotatingQuattro;
import dnacoders.dnaconvertors.RotatingTre;
import dnacoders.tree.coders.BPTreeNativeCoder;
import dnacoders.tree.sketchers.AbstractHashSketcher;
import dnacoders.tree.sketchers.IDNASketcher;
import dnacoders.tree.wrappers.node.EncodedNode;
import utils.Coder;
import utils.DNAPacker;
import utils.FuncUtils;
import utils.csv.BufferedCsvReader;
import utils.csv.BufferedCsvWriter;
import utils.csv.CsvLine;
import utils.lsh.minhash.MinHashLSH;
import java.util.*;
import java.util.stream.IntStream;

public class BTreeConfigSearch {
    public static void main(String[] args) {
        BufferedCsvReader reader = new BufferedCsvReader(args[0]);

        /*
        BufferedCsvWriter writer = new BufferedCsvWriter("configs_aggregated.csv", false);
        if (writer.isEmpty())
            writer.appendNewLine(
                    "payload size",
                    "b",
                    "c",
                    "bit rate internals",
                    "bit rate leaves",
                    "bit rate"
            );

         */


        Coder<List<Integer>, BaseSequence> keysCoder = new Coder<>() {
            @Override
            public BaseSequence encode(List<Integer> sortedList) {

                BaseSequence seq = new BaseSequence();
                int id0 = sortedList.get(0);
                int id;
                int size = sortedList.size();
                DNAPacker.packUnsigned(seq, id0);
                for (int i = 1; i < size; i++) {
                    id = sortedList.get(i);
                    DNAPacker.packUnsigned(seq, id - id0);
                    id0 = id;
                }

                return seq;
            }

            @Override
            public List<Integer> decode(BaseSequence seq) {
                DNAPacker.LengthBase lb = DNAPacker.LengthBase.parsePrefix(seq);
                List<Integer> keys = new ArrayList<>();
                int delta = lb.unpackSingle(seq, false).intValue();
                keys.add(delta);
                seq = seq.window(lb.totalSize());

                while (seq.length() > 0) {
                    lb = DNAPacker.LengthBase.parsePrefix(seq);
                    delta += lb.unpackSingle(seq, false).intValue();
                    keys.add(delta);
                    seq = seq.window(lb.totalSize());
                }

                return keys;
            }
        };
//        Coder<List<List<Integer>>, BaseSequence> valuesCoder = new Coder<>() {
//            @Override
//            public BaseSequence encode(List<List<Integer>> sortedList) {
//                var seq = new BaseSequence();
//                for (List<Integer> list : sortedList) {
//                    var encodedList = keysCoder.encode(list);
//                    DNAPacker.pack(seq, encodedList.length());
//                    seq.append(encodedList);
//                }
//                return seq;
//            }
//
//            @Override
//            public List<List<Integer>> decode(BaseSequence seq) {
//                List<List<Integer>> lists = new ArrayList<>();
//                int limit;
//                while(seq.length() > 0) {
//                    var lb = DNAPacker.LengthBase.parsePrefix(seq);
//                    int size = lb.unpackSingle(seq).intValue();
//                    var headerSize = lb.totalSize();
//                    limit = headerSize + size;
//                    lists.add(keysCoder.decode(seq.window(headerSize, limit)));
//                    seq = seq.window(limit);
//                }
//                return lists;
//            }
//        };
        Coder<List<String>, BaseSequence> valuesCoder = new Coder<>() {
            @Override
            public BaseSequence encode(List<String> csvLines) {
                return RotatingQuattro.INSTANCE.encode(String.join("\n", csvLines));
                //return RotatingTre.INSTANCE.encode(csvLines.stream().map(CsvLine::getLine).collect(Collectors.joining("\n")));
            }

            @Override
            public List<String> decode(BaseSequence seq) {
                return Arrays.stream(RotatingQuattro.INSTANCE.decode(seq).split("\n")).toList();
                //return Arrays.stream(RotatingTre.INSTANCE.decode(seq).split("\n")).map(line -> new CsvLine(line, colMapping)).toList();
            }
        };

        IDNASketcher<IDNASketch.HashSketch> sketcher = AbstractHashSketcher.builder().setFlavor(AbstractHashSketcher.Builder.Flavor.F1).setAddressSize(80).build();

        List<String> lines = reader.stream().limit(5_000).map(CsvLine::getLine).toList();
        reader.close();
        for (int payloadSize : Arrays.asList(250-80)) {
            BPTreeNativeCoder<Integer, String, IDNASketch.HashSketch> coder = new BPTreeNativeCoder.Builder<Integer, String, IDNASketch.HashSketch>()
                    .setPayloadSize(payloadSize)
                    .setToleranceFunctionLeaves(__ -> 0)
                    .setToleranceFunctionInternalNodes(__ -> 0)
                    .setLsh(MinHashLSH.newSeqLSHTraditional(6 , 5))
                    .setKeyCoder(keysCoder)
                    .setValueCoder(valuesCoder)
                    .setParallel(true)
                    .setSketcher(sketcher)
                    .build();



            double keySize = 8d;
            double pointerSize = 8d;
            for (var config : Arrays.asList(
                    new TreeConfig(170, 10, 2),
                    new TreeConfig(170, 2, 10)

            )) {

                    System.out.println("payloadSize: " + payloadSize + ", b: " + config.b + ", c: " + config.c);
                    System.out.println("creating tree");
                    BPlusTree<Integer, String> t = BPlusTree.bulkLoad(FuncUtils.zip(IntStream.range(0, lines.size()), lines.stream(), KVEntry::new), config.b, config.c);

                    System.out.println("encoding tree");
                    var encodedTree = coder.encode(t);

                    System.out.println("aggregating");

                    double bitsInternalNodes = t.stream().filter(n -> !n.isLeaf()).mapToDouble(n -> n.isAboveLeaf() ? keySize * n.size() + pointerSize * (2 + n.size()) : keySize * n.size() + pointerSize * (1 + n.size())).sum();
                    double bitsObjects= t.stream().filter(BPlusTree.Node::isLeaf).mapToLong(BPlusTree.Node::size).sum() * keySize
                            + lines.stream().mapToLong(String::length).sum();

                    int ntsInternals = encodedTree
                            .stream()
                            .filter(n -> !n.isLeaf())
                            .map(EncodedNode::payloads)
                            .mapToInt(a -> Arrays.stream(a).map(BaseSequence::length).reduce(0, Integer::sum))
                            .sum();
                    int ntsLeaves = encodedTree
                            .stream()
                            .filter(EncodedNode::isLeaf)
                            .map(EncodedNode::payloads)
                            .mapToInt(a -> Arrays.stream(a).map(BaseSequence::length).reduce(0, Integer::sum))
                            .sum();

                    /*
                    writer.appendNewLine(
                            payloadSize,
                            config.b,
                            config.c,
                            bitsInternalNodes / ntsInternals,
                            bitsObjects / ntsLeaves,
                            (bitsInternalNodes + bitsObjects) / (ntsInternals + ntsLeaves)
                    );

                     */


                    System.out.println("bit rate internals: " + (bitsInternalNodes / ntsInternals));
                    System.out.println("bit rate leaves: " + (bitsObjects / ntsLeaves));
                System.out.println();

            }
        }
        System.out.println();
        //writer.close();
    }

    record TreeConfig(int payloadSize, int b, int c) {

    }

    record TreeBitRate(double internals, double leaves) {

    }
}
