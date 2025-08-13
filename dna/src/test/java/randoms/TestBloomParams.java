package randoms;

import datastructures.hashtable.BloomFilter;

public class TestBloomParams {

    public static void main(String[] args) {
        double fpp = 0.001d;
        long nElements = 10_000_000_000L;
        System.out.println("fpp: " + fpp);
        System.out.println("n elements: " + nElements);
        System.out.println("num bits: " + BloomFilter.numBits(fpp, nElements) + " = " + (BloomFilter.numBits(fpp, nElements) / 8000000000f) + " GBs");
        System.out.println("num hash functions: " + BloomFilter.numHashFunctions(fpp));
        System.out.println("bits per element: " + BloomFilter.numBitsPerElement(BloomFilter.numBits(fpp, nElements), nElements));
        System.out.println("fpp: " + BloomFilter.falsePositiveProb(BloomFilter.numBits(fpp, nElements), nElements));
        System.out.println("fpp: " + BloomFilter.falsePositiveProb(BloomFilter.numHashFunctions(fpp), BloomFilter.numBits(fpp, nElements), nElements));
    }
}
