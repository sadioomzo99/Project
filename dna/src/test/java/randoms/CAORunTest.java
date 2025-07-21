package randoms;

import core.BaseSequence;
import dnacoders.dnaconvertors.RotatingTre;
import utils.*;

import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

public class CAORunTest {

    public static void main(String[] args) {
        Coder<byte[], String> stringCoder = Coder.of(bs -> Base64.getEncoder().encodeToString(bs), s -> Base64.getDecoder().decode(s));
        Coder<byte[], BaseSequence> byteCoder3 = Coder.fuse(
                stringCoder,
                RotatingTre.INSTANCE
        );

        Random rand = new Random();
        for (int i = 0; i < 100_000; i++) {
            byte[] bs = new byte[1 + rand.nextInt(500)];
            rand.nextBytes(bs);
            BaseSequence encoded = byteCoder3.encode(bs);
            byte[] decoded = byteCoder3.decode(encoded);
            if (!Arrays.equals(bs, decoded))
                throw new RuntimeException();
        }

    }
}
