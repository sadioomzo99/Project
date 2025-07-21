package randoms;

import core.Attribute;
import core.BaseSequence;
import core.dnarules.BasicDNARules;
import core.dnarules.DNARule;
import dnacoders.AttributeMapper;
import dnacoders.dnaconvertors.RotatingQuattro;
import dnacoders.headercoders.BasicDNAPadder;
import dnacoders.headercoders.PermutationCoder;
import utils.*;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TestHashSimilarities {

    public static void main(String[] args) {
        int n = 1000;
        BaseSequence seq = new BaseSequence("AAGGGCCTGGCCTGCCTTCATTCTCAGCACCAACTAATTATATAGTGTTTTAG");
        var coder = attributeEncoder(false, 80, 16, RotatingQuattro.INSTANCE, BasicDNARules.INSTANCE);
        List<BaseSequence> hashedSeqs = hashed(seq, n);
        List<BaseSequence> codedSeqs = encoded(coder, n);

        System.out.println(stats(hashedSeqs));
        System.out.println(stats(codedSeqs));
    }


    public static DoubleSummaryStatistics stats(List<BaseSequence> seqs) {
        return monotonePairingStream(seqs).parallel().flatMapToDouble(s -> s.parallel().mapToDouble(p -> p.getT1().hammingDistance(p.getT2()))).summaryStatistics();
    }

    public static BaseSequence h(BaseSequence seq) {
        var p = FuncUtils.getUniformPermutation(Long.parseLong(SeqBitStringConverter.transform(seq.window(seq.length() - 8)).toString(), 2), seq.length());
        return seq.permute(p);
    }

    public static <T> Stream<Stream<Pair<T, T>>> monotonePairingStream(List<T> ts) {
        return Stream.iterate(0, i -> i < ts.size(), i -> i + 1)
                .map(i -> IntStream.range(i + 1, ts.size())
                        .mapToObj(j -> new Pair<>(ts.get(i), ts.get(j))));
    }

    public static AsymmetricCoder<Attribute<?>, Attribute<String>, BaseSequence> attributeEncoder(boolean parallel, int targetLength, int permsCount, Coder<String, BaseSequence> dnaConvertor, DNARule rules) {
        AsymmetricCoder<Attribute<?>, Attribute<String>, BaseSequence> coder =  AsymmetricCoder.fuse(
                AttributeMapper.newInstance(dnaConvertor),
                new BasicDNAPadder(targetLength - DNAPacker.pack(permsCount - 1).length())
        );

        if (permsCount == 0)
            return coder;

        return AsymmetricCoder.fuse(
                coder,
                new PermutationCoder(parallel, permsCount, seq -> -rules.evalErrorProbability(seq))
        );
    }


    public static List<BaseSequence> hashed(BaseSequence start, int n) {
        return Stream.iterate(start, TestHashSimilarities::h).parallel().limit(n).collect(Collectors.toList());
    }

    public static List<BaseSequence> encoded(AsymmetricCoder<Attribute<?>, Attribute<String>, BaseSequence> coder, int n) {
        return IntStream.range(0, n).parallel().mapToObj(i -> coder.encode(new Attribute<>("id", i))).toList();
    }
}
