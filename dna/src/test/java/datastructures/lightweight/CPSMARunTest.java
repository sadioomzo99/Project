package datastructures.lightweight;

import core.BaseSequence;
import datastructures.KVEntry;
import datastructures.cola.run.CPSMARun;
import datastructures.container.impl.SizedDNAContainer;
import datastructures.container.translation.DNAAddrManager;
import dnacoders.dnaconvertors.RotatingTre;
import utils.Coder;
import utils.DNAPacker;
import utils.lsh.minhash.MinHashLSH;
import utils.lsh.storage.LSHStorage;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class CPSMARunTest {
    public static void main(String[] args) {

        DNAAddrManager atm = DNAAddrManager
                .builder()
                .setLsh(MinHashLSH.newSeqAmpLSHTraditional(5, 200, 20, LSHStorage.AmplifiedLSHStorage.Amplification.OR))
                .setAddrSize(80)
                .build();

        SizedDNAContainer container = SizedDNAContainer.builder().setAddressManager(atm).setPayloadSize(120).build();

        Random rand = new Random();
        long min = 400;
        long max = 10000;
        int count = 10;
        List<Long> sortedData = Stream.generate(() -> rand.nextLong(min, max)).limit(count).sorted().distinct().toList();

        Coder<KVEntry<Long, String>, BaseSequence> coder = Coder.of(e ->
                BaseSequence.join(
                        DNAPacker.pack(e.key(), DNAPacker.LengthBase.INT_64),
                        RotatingTre.INSTANCE.encode(e.value())),
                seq -> new KVEntry<>(
                        DNAPacker.LengthBase.INT_64.unpackSingle(seq, false).longValue(),
                        RotatingTre.INSTANCE.decode(seq.window(DNAPacker.LengthBase.INT_64.totalSize()))
                ));

        CPSMARun.Builder<String> runBuilder = new CPSMARun.Builder<>(count, 0, true, container, coder);
        runBuilder.addAll(sortedData.stream().map(l -> new KVEntry<>(l, "l=" + l)).toList());
        CPSMARun<String> run = runBuilder.build();

        System.out.println("run");
        System.out.println(run.stream().toList());

        long p = rand.nextLong(min, max);
        System.out.println("\npoint: " + p);

        System.out.println(run.search(p));

        long e = p + 1000;
        System.out.println("\nrange: [" + p + ", " + e + "]");
        System.out.println(run.searchRange(p, e).stream().toList());

    }
}
