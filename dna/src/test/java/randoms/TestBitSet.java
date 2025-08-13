package randoms;

import utils.BitSetXXL;
import java.util.BitSet;
import java.util.concurrent.ThreadLocalRandom;

public class TestBitSet {
    public static void main(String[] args) {
        int numBits = Integer.MAX_VALUE;
        long numBitsLong = Integer.MAX_VALUE;
        BitSet bs1 = new BitSet(numBits);
        long factor = 2L;


        BitSetXXL bs2 = new BitSetXXL(numBitsLong * Math.max(1L, factor));
        long factor_1 = Math.max(0L, factor - 1);
        for (int i = 0; i < 1000_000; i++) {
            int pos = ThreadLocalRandom.current().nextInt(numBits);
            bs1.set(pos);
            bs2.set(pos + factor_1 * numBitsLong);
        }

        for (int i = 0; i < numBits; i++) {
            if (bs1.get(i) != bs2.get(i + factor_1 * numBitsLong))
                throw new RuntimeException("DAYUM");

        }
        System.out.println("yo");
    }
}
