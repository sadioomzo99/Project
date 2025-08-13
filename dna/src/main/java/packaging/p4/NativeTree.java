package packaging.p4;

import core.BaseSequence;
import datastructures.reference.IDNAFedReference;
import datastructures.reference.IDNASketch;
import datastructures.KVEntry;
import datastructures.searchtrees.BPlusTree;
import dnacoders.tree.coders.BPTreeNativeCoder;
import dnacoders.tree.sketchers.AbstractHashSketcher;
import dnacoders.tree.sketchers.IDNASketcher;
import dnacoders.tree.wrappers.tree.EncodedBPTree;
import utils.Coder;
import utils.DNAPacker;
import utils.lsh.minhash.MinHashLSH;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class NativeTree {

    public static void main(String[] args) {
        var b = 2;
        var c = 2;

        var list = IntStream.range(0, 15).boxed().toList();
        System.out.println("inserting list: " + list);
        BPlusTree<Integer, Integer> tree = BPlusTree.bulkLoad(list.stream().map(e -> new KVEntry<>(e, e)), b, c);
        tree.printLevelOrder();
        System.out.println();

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

        int addressSize = 80;
        int payloadSize = 10000;

        IDNASketcher<IDNASketch.HashSketch> sketcher = AbstractHashSketcher.builder().setFlavor(AbstractHashSketcher.Builder.Flavor.F2).setAddressSize(addressSize).build();
        BPTreeNativeCoder<Integer, Integer, IDNASketch.HashSketch> coder = new BPTreeNativeCoder.Builder<Integer, Integer, IDNASketch.HashSketch>()
                .setPayloadSize(payloadSize)
                .setToleranceFunctionLeaves(__ -> 0)
                .setToleranceFunctionInternalNodes(__ -> 0)
                .setLsh(MinHashLSH.newSeqLSHTraditional(6 , 5))
                .setKeyCoder(keysCoder)
                //.setValueCoder(keysCoder)
                //.setSeedTrialsAndTargetScore(3, 0.5f)
                .setSeedTrialsWithMaxScore(2)
                .setParallel(true)
                .setSketcher(sketcher)
                .build();

        EncodedBPTree<Integer, Integer, BaseSequence[], IDNASketch.HashSketch> test = coder.encode(tree);
        test.search(7, 10).forEach(System.out::println);
        System.out.println();
        test.searchLeafIterator(9).forEachRemaining(en -> System.out.println(test.decode(en)));
        System.out.println();

        System.out.println("decoded:");
        test.getEncodedNodeStorage().stream().map(en -> en.asFedReference(coder)).map(IDNAFedReference::decode).forEach(System.out::println);

        System.out.println();


        int nucleotides = Arrays.stream(test.getRoot().joinedOligos()).mapToInt(BaseSequence::length).sum();
        int pointers = test.getRoot().size();

        System.out.println("nucleotides: " + nucleotides);
        System.out.println("pointers: " + pointers);
        System.out.println("bit rate: " + (pointers * 4d * 4d) / nucleotides);
    }
}
