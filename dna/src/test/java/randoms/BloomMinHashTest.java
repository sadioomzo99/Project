package randoms;

import core.BaseSequence;
import utils.lsh.LSH;
import utils.lsh.minhash.MinHashLSH;

public class BloomMinHashTest {

    public static void main(String[] args) {
        LSH<BaseSequence> lsh = MinHashLSH.newSeqLSHTraditional(5, 3);
        BaseSequence seq1 = new BaseSequence("CCCCCCCC");
        BaseSequence seq2 = new BaseSequence("CCCCCCA");
        lsh.insert(seq1);
        System.out.println(lsh.query(seq2));
        System.out.println(1f - seq1.jaccardDistance(seq2, 4));
    }
}
