package randoms;

import core.BaseSequence;
import utils.lsh.minhash.MinHashLSH;
import utils.lsh.storage.LSHStorage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class LSHTest {
    public static void main(String[] args) throws Exception{
        List<BaseSequence> seqs = new ArrayList<>(List.of(
                new BaseSequence("ACGGGATGGGTGCCGGAT"),
                new BaseSequence("ACGGATCGGGGTACGAGT"),
                new BaseSequence("CCGGGATCGGGAGTCCGAT"),
                new BaseSequence("TCGGGATCAGGTAGCGATA"),
                new BaseSequence("CCGGGATCGGGAGTCCGAT"),
                new BaseSequence("ACGGGATCGGGAGTCCGAT"),
                new BaseSequence("CAGGGATCGGGAGTCCGAT")
        ));
        seqs.addAll(Stream.generate(() -> BaseSequence.random(10, 0.5)).limit(100_000).toList());
        MinHashLSH.Traditional<BaseSequence> index = MinHashLSH.newSeqAmpLSHTraditional(4, 200, 20, LSHStorage.AmplifiedLSHStorage.Amplification.OR);
        long t1 = System.currentTimeMillis();
        index.insertParallel(seqs);
        System.out.println("time=" + (System.currentTimeMillis() - t1) / 1000f + " secs");
        var h1 = index.minHashesPerHashFunction(seqs.get(5));
        var h2 = index.minHashesPerHashFunction(seqs.get(6));

        System.out.println("true sim=" + (1 - seqs.get(5).jaccardDistance(seqs.get(6),index.getK())));
        System.out.println("estimated sim by minHash=" + IntStream.range(0, h1.length).filter(i -> h1[i] == h2[i]).count() / (float) h1.length);
        System.out.println("calculate sim seqs=" + index.candidates(seqs.get(5)));
    }
}
