package reedsolomon;

import core.BaseSequence;
import dnacoders.headercoders.ReedSolomonCoder;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

public class RSCConsistencyTest {

    public static void main(String[] args) {
        ReedSolomonCoder coder = new ReedSolomonCoder(2);

        var result = Stream.generate(() -> BaseSequence.random(5 + ThreadLocalRandom.current().nextInt(50)))
                .parallel()
                .limit(100_000)
                .allMatch(s -> coder.decode(coder.encode(s)).equals(s));

        System.out.println(result);
    }
}
