package packaging.p4;

import core.BaseSequence;
import datastructures.container.DNAContainer;
import datastructures.reference.IDNASketch;
import datastructures.KVEntry;
import datastructures.searchtrees.BPlusTree;
import dnacoders.tree.coders.BPTreeContainerCoder;
import dnacoders.tree.wrappers.tree.EncodedBPTree;
import utils.Coder;
import utils.DNAPacker;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class ContainerTree {

    public static void main(String[] args) {
        var b = 20;
        var c = 40;

        var list = IntStream.range(0, 2000).boxed().toList();
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

        BPTreeContainerCoder<Integer, Integer> coder = new BPTreeContainerCoder<>(DNAContainer.builder().build(), keysCoder, keysCoder);
        EncodedBPTree<Integer, Integer, Long, IDNASketch.ContainerIdSketch> test = coder.encode(tree);
        test.search(4, 10).forEach(System.out::println);
        System.out.println();
        test.searchLeafIterator(7).forEachRemaining(en -> System.out.println(test.decode(en)));
        System.out.println();

        System.out.println(test.decode(test.getRoot()));

        System.out.println("decoded:");
        test.getEncodedNodeStorage().stream().map(en -> en.decode(coder)).forEach(System.out::println);
    }
}
