package rq;

import core.dnarules.SuperBasicDNARules;
import dnacoders.headercoders.PermutationCoder;
import utils.Coder;
import utils.compression.GZIP;
import utils.rq.RQCoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class RaptorDna {
    public static void main(String[] args) {
        final float maxSuperBasicError = 0.4f;
        SuperBasicDNARules rules = SuperBasicDNARules.INSTANCE;
        PermutationCoder permutationCoder = new PermutationCoder(false, 64, seq -> -rules.evalErrorProbability(seq));
        var rqCoder = Coder.fuse(
                new GZIP(),
                new RQCoder(seq -> rules.evalErrorProbability(seq) <= maxSuperBasicError,
                        seq -> rules.evalErrorProbability(seq) <= maxSuperBasicError));

        String org;
        String decoded1;

        var encodedLengths = IntStream.builder();
        var permutedLengths = IntStream.builder();

        var errorRQ = DoubleStream.builder();
        var errorPermuted = DoubleStream.builder();

        byte[] bytes;
        long t = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            org = randomString();
            bytes = org.getBytes(StandardCharsets.UTF_8);

            var rqSeq = rqCoder.encode(bytes);
            var rqPlusPermutedSeq = permutationCoder.encode(rqSeq);


            encodedLengths.accept(rqSeq.length());
            permutedLengths.accept(rqPlusPermutedSeq.length());


            decoded1 = new String(rqCoder.decode(permutationCoder.decode(rqPlusPermutedSeq)));
            if (!decoded1.equals(org))
                throw new RuntimeException("org=" + org + "\ndecodedRQ=" + decoded1);



            errorRQ.accept(rules.evalErrorProbability(rqSeq));
            errorPermuted.accept(rules.evalErrorProbability(rqPlusPermutedSeq));

        }

        System.out.println("time=" + (System.currentTimeMillis() - t) / 1000f + " seconds");
        System.out.println("RQ length:\n" + encodedLengths.build().summaryStatistics());
        System.out.println("\nRQ + permuted length:\n" + permutedLengths.build().summaryStatistics());

        System.out.println("\nerror RQ:\n" + errorRQ.build().summaryStatistics());
        System.out.println("\nerror RQ + permuted:\n" + errorPermuted.build().summaryStatistics());

    }

    public static String randomString() {
        int len = 5 + ThreadLocalRandom.current().nextInt(95);
        return ThreadLocalRandom.current().ints(0, 256)
                .limit(len)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
