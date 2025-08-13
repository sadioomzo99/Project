package randoms;

import core.BaseSequence;
import dnacoders.GCFiller;

public class GCFillerTest {
    public static void main(String[] args) {
        BaseSequence seq = new BaseSequence("GCAATGGGCCCGGTG");
        System.out.println("seq len: " + seq.length());
        System.out.println("seq GC: " + seq.gcContent());
        BaseSequence filler = GCFiller.getTrimmedFiller(seq, 10);
        System.out.println("filler GC: " + filler.gcContent());

        System.out.println("seq+filler GC: " + new BaseSequence(seq, filler).gcContent());
    }
}
