package randoms;

import core.BaseSequence;
import dnacoders.headercoders.BasicDNAPadder;

public class PaddingByDelimTest {
    public static void main(String[] args) {

        test();

    }

    public static void test() {
        BasicDNAPadder c = new BasicDNAPadder(60);
        for (int i = 0; i < 1000; i++) {
            BaseSequence seq = BaseSequence.random(200);
            BaseSequence encoded = c.encode(seq);
            BaseSequence decoded = c.decode(encoded);
            if (!seq.equals(decoded))
                throw new RuntimeException("inconsistent at source=" + seq + "\nencoded=" + encoded + "\ndecoded=" + decoded);
        }
    }
}
