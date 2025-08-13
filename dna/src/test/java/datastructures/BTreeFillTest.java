package datastructures;

import datastructures.searchtrees.BPlusTree;
import utils.FuncUtils;
import java.util.Iterator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BTreeFillTest {
    public static void main(String[] args) {
        int min = 0;
        int max = 1000;
        int b = 40;
        int c = 20;
        Iterator<Stream<Integer>> streamIterator = FuncUtils.copy(IntStream.range(min, max).boxed()).iterator();
        BPlusTree<Integer, Integer> tree1 = BPlusTree.bulkLoad(streamIterator.next().parallel().map(i -> new KVEntry<>(i, i)), b, c);
        BPlusTree<Integer, Integer> tree2 = new BPlusTree<>(b, c);

        streamIterator.next().forEach(i -> tree2.insert(i, i));
        System.out.println("count: " + tree1.size());
        System.out.println("height: " + tree1.getHeight());
        System.out.println("nodes: " + tree1.getNumNodes());
        tree1.printLevelOrder();

        System.out.println();
        System.out.println("count: " + tree2.size());
        System.out.println("height: " + tree2.getHeight());
        System.out.println("nodes: " + tree2.getNumNodes());
        tree2.printLevelOrder();
    }
}
