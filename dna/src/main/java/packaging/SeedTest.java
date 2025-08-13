package packaging;

import core.Attribute;
import core.BaseSequence;
import core.dnarules.BasicDNARules;
import dnacoders.AttributeMapper;
import dnacoders.DistanceCoder;
import dnacoders.dnaconvertors.RotatingQuattro;
import utils.lsh.LSH;
import utils.lsh.minhash.MinHashLSH;
import utils.lsh.storage.LSHStorage;
import java.util.List;
import java.util.stream.IntStream;

public class SeedTest {

    public static void main(String[] args) {
        var coder = AttributeMapper.attributeEncoder(false, 80, 16, RotatingQuattro.INSTANCE, BasicDNARules.INSTANCE);
        List<BaseSequence> seqs = IntStream.range(0, 100_000).parallel().mapToObj(i -> new Attribute<>("Id", i)).map(coder::encode).toList();
        LSH<BaseSequence> lsh = MinHashLSH.newSeqAmpLSHTraditional(4, 200, 20, LSHStorage.AmplifiedLSHStorage.Amplification.OR);
        lsh.insertParallel(seqs);
        System.out.println("dists: " + seqs.stream().parallel().mapToDouble(seq -> DistanceCoder.distanceScoreExclusive(seq, lsh)).summaryStatistics());
    }
}
