package randoms;

import datastructures.hashtable.BloomFilter;

public class BFTest {

    public static void main(String[] args) {
        int nElements = 10_000_000;
        double fpp = 0.1;
        int numHashFunctions = 1;

        long numBitsTest = BloomFilter.numBits(nElements, fpp, numHashFunctions);
        System.out.println(numBitsTest);

        System.out.println(BloomFilter.falsePositiveProb(numHashFunctions, numBitsTest, nElements));

        System.out.println(BloomFilter.numBits(0.01d, 10_000_000L));

        System.out.println(BloomFilter.numHashFunctions(95850584, 10_000_000L));
    }
}
