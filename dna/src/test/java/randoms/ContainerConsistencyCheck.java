package randoms;

import core.BaseSequence;
import datastructures.container.DNAContainer;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class ContainerConsistencyCheck {
    public static void main(String[] args) {
        DNAContainer container = DNAContainer.builder().build();

        AtomicInteger counter = new AtomicInteger(0);
        IntStream.range(0, 10_000).parallel().forEach(i -> {
            BaseSequence seq = BaseSequence.random(ThreadLocalRandom.current().nextInt(1, 20_000), ThreadLocalRandom.current().nextDouble(0d, 1d));
            long id = container.registerId();
            container.put(id, seq);
            if (!container.get(id).equals(seq))
                throw new RuntimeException(seq.toString());
            System.out.println(counter.incrementAndGet());
        });

        System.out.println("done");
    }
}
