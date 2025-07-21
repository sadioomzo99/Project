package randoms;

import core.BaseSequence;
import utils.lsh.LSH;
import utils.lsh.minhash.MinHashLSH;

public class LSHLightTest {

    public static void main(String[] args) {
        int k = 3;
        LSH<BaseSequence> lsh1 = MinHashLSH.newSeqLSHLight(k, 5);
        LSH<BaseSequence> lsh2 = MinHashLSH.newSeqLSHTraditional(k, 5);

        BaseSequence seq1 = new BaseSequence("ACGATGGCGATGCGATAGAAGAACTCA");
        BaseSequence seq2 = new BaseSequence("GCGATGGGGATGCTATAGAAGAACTCA");
        lsh1.insert(seq2);
        lsh2.insert(seq2);
        System.out.println(1.0f - seq1.jaccardDistance(seq2, k));
        System.out.println(lsh1.query(seq1));
        System.out.println(lsh2.query(seq1));
    }
}
