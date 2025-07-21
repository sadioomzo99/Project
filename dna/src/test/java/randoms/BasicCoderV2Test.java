package randoms;

import core.Attribute;
import core.BaseSequence;
import core.dnarules.BasicDNARules;
import dnacoders.AttributeMapper;
import dnacoders.dnaconvertors.RotatingQuattro;
import dnacoders.headercoders.PermutationCoder;
import utils.AsymmetricCoder;
import utils.FuncUtils;
import java.util.concurrent.ThreadLocalRandom;

public class BasicCoderV2Test {

    public static void main(String[] args) {
        test();
    }

    public static void test() {
        AsymmetricCoder<Attribute<?>, Attribute<String>, BaseSequence> coder = AsymmetricCoder.fuse(
                AttributeMapper.newInstance(RotatingQuattro.INSTANCE),
                new PermutationCoder(true, 64, seq -> -BasicDNARules.INSTANCE.evalErrorProbability(seq))
        );


        for (int i = 0; i < 1000; i++) {
            Attribute<?> a = new Attribute<>(randomString(), randomString());
            BaseSequence coded = FuncUtils.safeCall(() -> coder.encode(a));
            if (coded == null)
                throw new RuntimeException("failed encoding a = " + a);
            Attribute<?> recovered = FuncUtils.safeCall(() -> coder.decode(coded));
            if (recovered == null)
                throw new RuntimeException("failed decoding a = " + a);
            if (!a.equals(recovered))
                throw new RuntimeException("a=" + a);
        }

        System.out.println("fine!");
    }

    public static String randomString() {
        int len = 1 + ThreadLocalRandom.current().nextInt(5);
        int leftLimit = '0';
        int rightLimit = 'z';

        return ThreadLocalRandom.current().ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(len)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
