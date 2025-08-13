package randoms;

import core.BaseSequence;
import core.dnarules.BasicDNARules;
import dnacoders.PayloadDistanceCoder;
import utils.AddressedDNA;
import utils.lsh.minhash.MinHashLSH;

import java.util.concurrent.ThreadLocalRandom;

public class PayloadDistCoderTest {

    public static void main(String[] args) {
        PayloadDistanceCoder pdc = new PayloadDistanceCoder(
                MinHashLSH.newSeqLSHTraditional(5, 5),
                BasicDNARules.INSTANCE,
                0,
                1f,
                1f
        );

        for (int i = 0; i < 1000_000; i++) {
            BaseSequence addr = BaseSequence.random(80);
            BaseSequence payload = BaseSequence.random(ThreadLocalRandom.current().nextInt(1, 250));
            AddressedDNA ad = new AddressedDNA(addr, payload);
            if (!pdc.decode(pdc.encode(ad)).equals(ad)) {
                throw new RuntimeException("" + ad);
            }

        }

    }
}
