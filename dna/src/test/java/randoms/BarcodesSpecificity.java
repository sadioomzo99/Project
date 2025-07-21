package randoms;

import core.Attribute;
import core.BaseSequence;
import core.dnarules.BasicDNARules;
import dnacoders.AttributeMapper;
import dnacoders.dnaconvertors.RotatingQuattro;
import dnacoders.headercoders.PermutationCoder;
import utils.AsymmetricCoder;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class BarcodesSpecificity {

    public static void main(String[] args) {
        test();
    }

    static void test() {
        AsymmetricCoder<Attribute<?>, Attribute<String>, BaseSequence> coder = AsymmetricCoder.fuse(
                AttributeMapper.newInstance(RotatingQuattro.INSTANCE),
                new PermutationCoder(true, 64, seq -> -BasicDNARules.INSTANCE.evalErrorProbability(seq))
        );

        List<BaseSequence> barcodes = IntStream.range(0, 10).boxed().parallel()
                .flatMap(id -> IntStream.range(0, 10)
                        .mapToObj(i -> new Attribute<>("pk", id + "." + i))
                        .map(coder::encode))
                .toList();

        System.out.println("d_edit(" + coder.decode(barcodes.get(0)) + ", " + coder.decode(barcodes.get(1)) + ")=" + barcodes.get(0).editDistance(barcodes.get(1)));
        printDistanceSummary(barcodes, 4);
    }

    public static void printDistanceSummary(List<BaseSequence> seqs, int k) {
        DoubleStream.Builder s = DoubleStream.builder();
        for (int i = 0; i < seqs.size(); i++) {
            for (int j = i + 1; j < seqs.size(); j++) {
                s.accept(seqs.get(i).jaccardDistance(seqs.get(j), k));
            }
        }

        System.out.println(s.build().summaryStatistics());
    }
}
