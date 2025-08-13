package randoms;

import core.BaseSequence;
import utils.DNAPacker;

public class BaseSequencePackerTest {
    public static void main(String[] args) {
        BaseSequence seq = new BaseSequence();
        DNAPacker.pack(seq, 5, 20, 88888, 1, 0, -200000, Integer.MAX_VALUE, -121212);
        var vals = DNAPacker.unpackAll(seq);
        System.out.println(seq);
        for (var val : vals) {
            System.out.println(val);
        }

        System.out.println("streamed:");
        DNAPacker.unpackAllStream(seq, true).forEach(System.out::println);
    }
}
