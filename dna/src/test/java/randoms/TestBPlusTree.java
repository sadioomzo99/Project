package randoms;

import datastructures.searchtrees.BPlusTree;
import utils.FuncUtils;

public class TestBPlusTree {
    public static void main(String[] args) {
        BPlusTree<Integer, String> tree = new BPlusTree<>(2, 2);
        for (int i = 0; i < 15; i++) {
            tree.insert(i, "i=" + i);
        }
        tree.printLevelOrder();

        System.out.println();
        FuncUtils.stream(tree::bottomUpLevelIterator).forEach(System.out::println);
        System.out.println();
        tree.stream().forEach(System.out::println);
    }
}
