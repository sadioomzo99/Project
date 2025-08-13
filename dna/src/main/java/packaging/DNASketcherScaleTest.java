package packaging;

import core.BaseSequence;
import core.dnarules.BasicDNARules;
import datastructures.hashtable.BloomFilter;
import datastructures.reference.IDNASketch;
import dnacoders.tree.sketchers.AbstractHashSketcher;
import dnacoders.tree.sketchers.IDNASketcher;
import utils.FuncUtils;
import utils.lsh.LSH;
import utils.lsh.minhash.MinHashLSH;
import java.util.Arrays;

public class DNASketcherScaleTest {

    public static void main(String[] args) {
        int count = FuncUtils.tryOrElse(() -> Integer.parseInt(args[0]), () -> 100);
        int tol = FuncUtils.tryOrElse(() -> Integer.parseInt(args[1]), () -> 0);
        int k = FuncUtils.tryOrElse(() -> Integer.parseInt(args[2]), () -> 5);
        int r = FuncUtils.tryOrElse(() -> Integer.parseInt(args[3]), () -> 5);

        long numBits = count * 2L;
        System.out.println("BitMinHashLSH with:");
        System.out.println("nElements: " + count);
        System.out.println("numBits: " + numBits);
        System.out.println("bits/element: " + numBits / (double) count);

        System.out.println();
        LSH<BaseSequence> lsh = MinHashLSH.newSeqLSHBloom(k, r, numBits, BloomFilter.numHashFunctions(0.001d));
        IDNASketcher<?> sketcher = AbstractHashSketcher.builder().setFlavor(AbstractHashSketcher.Builder.Flavor.F1).setAddressSize(80).setLsh(lsh).build();
        IDNASketch sketch = sketcher.createSketch(count, tol);
        System.out.println("generated sketch of length: " + count);

        System.out.println("length: " + Arrays.stream(sketch.addresses()).parallel().mapToInt(BaseSequence::length).summaryStatistics());
        System.out.println("error:  " + Arrays.stream(sketch.addresses()).parallel().mapToDouble(BasicDNARules.INSTANCE::evalErrorProbability).summaryStatistics());
    }
}
