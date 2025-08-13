package reedsolomon;

import core.BaseSequence;
import dnacoders.headercoders.ReedSolomonCoder;

public class RSTest {

    public static void main(String[] args) {
        ReedSolomonCoder rsc = new ReedSolomonCoder(1);
        for (int ecc = 1; ecc < 9; ecc++) {
            for (int i = 1; i < 150; i++) {
                BaseSequence seq = BaseSequence.random(i);
                var encoded = rsc.encode(seq, ecc);
                if ((encoded.length() - seq.length() - ReedSolomonCoder.overhead(seq.length(), ecc)) != 0)
                    throw new RuntimeException();
            }
        }
        System.out.println("Overhead is calculated correctly");
    }
}
