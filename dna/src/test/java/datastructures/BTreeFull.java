package datastructures;

import datastructures.searchtrees.BPlusTree;
import java.util.stream.IntStream;

public class BTreeFull {

    public static void main(String[] args) {
        int b = 2;
        int c = 2;
        int leafSize = 2 * c - 1;
        int internalNodeSize = 2 * b - 1;
        int h = 5;
        int numberTuples = leafSize * (int) Math.pow(internalNodeSize, h);
        BPlusTree<Integer, Integer> tree = BPlusTree.bulkLoad(IntStream.range(0, numberTuples).mapToObj(i -> new KVEntry<>(i, i)), b, c);
        tree.printLevelOrder();
        System.out.println();

        int numLeafNodes = numberTuples / leafSize;
        int _numInternalNodes = (int) Math.pow(internalNodeSize, h - 1);
        int numInternalNodes = numLeafNodes / (int) (Math.pow(internalNodeSize, h - 1));

        //System.out.println("#                 = " + numberTuples + " : " + tree.size());
        //System.out.println("h                 = " + h + " : " + (tree.getHeight() - 1));
        System.out.println("numInternalNodes  = " + numInternals(b, c, h) + " <--> " +  __numInternals(b, h)  + " : " + tree.stream().filter(n -> !n.isLeaf()).count());
        //System.out.println("numLeafNodes      = " + numLeafNodes + " : " + tree.stream().filter(BPlusTree.Node::isLeaf).count());
        System.out.println();



    }

    static int numInternals(int b, int c, int h) {
        if (h == 0)
            return 0;
        if (h == 1)
            return 1;

        int leafSize = 2 * c - 1;
        int internalNodeSize = 2 * b - 1;

        int numberTuples = leafSize * (int) Math.pow(internalNodeSize, h);

        int n_h = numberTuples / leafSize; // num leaves
        int n_h_1;
        int sum = 0;
        for (int i = h; i > 1; i--) {
            n_h_1 = n_h / internalNodeSize;
            sum += n_h_1;
            n_h = n_h_1;
        }
        return 1 + sum;
    }

    static int _numInternals(int b, int h) {
        if (h == 0)
            return 0;
        if (h == 1)
            return 1;

        int sum = 0;
        int fullNodeSize = 2 * b - 1;
        for (int i = 1; i < h; i++)
            sum += (int) Math.pow(fullNodeSize, i);

        return 1 + sum;
    }

    static int __numInternals(int b, int h) {
        if (h == 0)
            return 0;

        int fullNodeSize = 2 * b - 1;
        return (int) (Math.pow(fullNodeSize, h) - 1) / (fullNodeSize - 1);
    }
}
