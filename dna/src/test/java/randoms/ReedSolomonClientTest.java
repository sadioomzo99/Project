package randoms;

import core.BaseSequence;
import utils.Pair;
import utils.ReedSolomonClient;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class ReedSolomonClientTest {
    public static void main(String[] args) {
        int RUNS = 10;
        List<Pair<BaseSequence, BaseSequence>> testSequences = new ArrayList<>();
        ReedSolomonClient client = ReedSolomonClient.getInstance();
        var startTime = System.nanoTime();
        IntStream.range(0, RUNS).parallel().forEach(i -> {
            var bs = BaseSequence.random(20);
            var ecc = client.encode(i, 2, bs);
            testSequences.add(new Pair<>(bs, ecc));
//            System.out.println(client.ecc(i, BaseSequence.random(20)));
        });
        var endTime = System.nanoTime();
        System.out.println("TIME: " + (endTime - startTime) / 1000000);
        testSequences.forEach(pair -> {
            var copy = pair.getT1().clone();
            copy.append(pair.getT2());
            var dist = client.decode(copy, 2).hammingDistance(pair.getT1());
            System.out.println(dist);
        });
    }
}
