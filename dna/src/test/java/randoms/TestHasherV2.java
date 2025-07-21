package randoms;

import core.BaseSequence;
import core.dnarules.BasicDNARules;
import datastructures.reference.IDNASketch;
import dnacoders.DistanceCoder;
import dnacoders.tree.sketchers.AbstractHashSketcher;
import dnacoders.tree.sketchers.IDNASketcher;
import utils.lsh.LSH;
import utils.lsh.minhash.MinHashLSH;
import utils.lsh.storage.LSHStorage;
import java.util.Arrays;
import java.util.List;

public class TestHasherV2 {
    public static void main(String[] args) {
        IDNASketcher<IDNASketch.HashSketch> sketcher2 = AbstractHashSketcher.builder().setFlavor(AbstractHashSketcher.Builder.Flavor.F2).setAddressSize(80).build();
        IDNASketcher<IDNASketch.HashSketch> sketcher1 = AbstractHashSketcher.builder().setFlavor(AbstractHashSketcher.Builder.Flavor.F1).setAddressSize(80).build();


        IDNASketch.HashSketch sketch1 = sketcher1.createSketch(5_000, 50);
        IDNASketch.HashSketch sketch2 = sketcher2.createSketch(5_000, 50);


        List<BaseSequence> addresses1 = Arrays.asList(sketch1.addresses());
        List<BaseSequence> addresses2 = Arrays.asList(sketch2.addresses());

        System.out.println("error V1: " + addresses1.stream().parallel().mapToDouble(BasicDNARules.INSTANCE::evalErrorProbability).summaryStatistics());
        System.out.println("error V2: " + addresses2.stream().parallel().mapToDouble(BasicDNARules.INSTANCE::evalErrorProbability).summaryStatistics());

        LSH<BaseSequence> lsh1 = MinHashLSH.newSeqAmpLSHTraditional(5, 20, 4, LSHStorage.AmplifiedLSHStorage.Amplification.OR);
        lsh1.insertParallel(addresses1);
        System.out.println("dist V1: " + addresses1.stream().parallel().mapToDouble(seq -> Math.min(DistanceCoder.distanceScoreExclusive(seq, lsh1), DistanceCoder.distanceScore(seq.complement(), lsh1))).summaryStatistics());

        LSH<BaseSequence> lsh2 = MinHashLSH.newSeqAmpLSHTraditional(5, 20, 4, LSHStorage.AmplifiedLSHStorage.Amplification.OR);
        lsh2.insertParallel(addresses2);
        System.out.println("dist V2: " + addresses2.stream().parallel().mapToDouble(seq -> Math.min(DistanceCoder.distanceScoreExclusive(seq, lsh2), DistanceCoder.distanceScore(seq.complement(), lsh2))).summaryStatistics());
    }
}
