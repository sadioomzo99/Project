package randoms;

import core.BaseSequence;
import datastructures.KVEntry;
import datastructures.container.DNAContainer;
import datastructures.searchtrees.BPlusTree;
import dnacoders.tree.coders.BPTreeContainerCoder;
import utils.Coder;
import utils.DNAPacker;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class TestTreeCoders {

    public static void main(String[] args) {
        var b = 20;
        var c = 20;
        int count = 100000;
        BPlusTree<Integer, Integer> tree = BPlusTree.bulkLoad(IntStream.range(0, count).mapToObj(e -> new KVEntry<>(e, e)), b, c);
        //tree.printLevelOrder();
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
        var cont = coder.encode(tree).getEncodedNodeStorage();
        //cont.collect().forEach(System.out::println);
        System.out.println("root: " + cont.getRoot());

    }
}
