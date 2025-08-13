package randoms;

import core.Attribute;
import core.BaseSequence;
import core.dnarules.BasicDNARules;
import dnacoders.AttributeMapper;
import dnacoders.DistanceCoder;
import dnacoders.dnaconvertors.RotatingQuattro;
import dnacoders.headercoders.PermutationCoder;
import utils.AsymmetricCoder;
import utils.lsh.LSH;
import utils.lsh.minhash.MinHashLSH;
import utils.lsh.storage.LSHStorage;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public class ProbesBooksPage {

    public static void main(String[] args) {
        AsymmetricCoder<Attribute<?>, Attribute<String>, BaseSequence> coder = AsymmetricCoder.fuse(
                AttributeMapper.newInstance(RotatingQuattro.INSTANCE),
                new PermutationCoder(true, 64, seq -> -BasicDNARules.INSTANCE.evalErrorProbability(seq))
        );

        LSH<BaseSequence> lsh = MinHashLSH.newSeqAmpLSHTraditional(4, 200, 20, LSHStorage.AmplifiedLSHStorage.Amplification.OR);

        long t = System.currentTimeMillis();
        List<BaseSequence> probes = IntStream.rangeClosed(1, 1000).parallel().mapToObj(i -> coder.encode(new Attribute<>("page", i))).peek(lsh::insert).toList();
        System.out.println(probes.stream().parallel().mapToDouble(p -> DistanceCoder.distanceScore(p, lsh)).summaryStatistics());
        System.out.println("time: " + (System.currentTimeMillis() - t) / 1000f + " seconds");
    }


    private static float jaccardDistance(Set<BaseSequence> kmers1, List<BaseSequence> kmers2) {
        Set<BaseSequence> union = new HashSet<>(kmers1);
        Set<BaseSequence> intersection = new HashSet<>(kmers1);
        union.addAll(kmers2);
        intersection.retainAll(kmers2);
        return 1.0f - ((float) intersection.size() / union.size());
    }
}
