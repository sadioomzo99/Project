package datastructures;

import core.BaseSequence;
import datastructures.cola.COLA;
import datastructures.cola.PartitionedCOLA;
import datastructures.cola.run.managers.BasicRunsManager;
import datastructures.cola.run.managers.RunsManager;
import datastructures.cola.runbuilderstrategy.RunBuilderStrategy;
import datastructures.cola.searchstrategy.COLARangeQueryStrategy;
import datastructures.cola.searchstrategy.COLAStabbingQueryStrategy;
import datastructures.container.DNAContainer;
import datastructures.container.impl.SizedDNAContainer;
import datastructures.container.translation.DNAAddrManager;
import dnacoders.dnaconvertors.RotatingQuattro;
import utils.Coder;
import utils.lsh.minhash.MinHashLSH;
import utils.lsh.storage.LSHStorage;

public class DNARunsManagerTest {

    public static void main(String[] args) {
        Coder<KVEntry<Integer, Integer>, BaseSequence> entryCoder = Coder.fuse(
                Coder.of(e -> e.key() + ";" + e.value(), (String s) -> {
                    var split = s.split(";");
                    return new KVEntry<>(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                }),
                RotatingQuattro.INSTANCE
        );

        int startDNALevels = 3;
        DNAAddrManager atm = DNAAddrManager
                .builder()
                .setLsh(MinHashLSH.newSeqAmpLSHTraditional(5, 200, 20, LSHStorage.AmplifiedLSHStorage.Amplification.OR))
                .setAddrSize(80)
                .build();
        DNAContainer container = SizedDNAContainer.builder().setAddressManager(atm).setPayloadSize(120).build();
        RunsManager<Integer, Integer> rm = new BasicRunsManager<>(RunBuilderStrategy.partitionedDNA_CAO(true, startDNALevels, container, entryCoder));
        COLA<Integer, Integer> cola = new PartitionedCOLA<>(rm, startDNALevels, true);
        cola.insert(1, 1);
        cola.insert(2, 2);
        cola.insert(3, 3);
        cola.insert(5, 5);
        cola.insert(6, 6);
        cola.insert(8, 8);
        cola.insert(9, 9);
        cola.insert(10, 10);
        cola.insert(12, 12);
        cola.insert(14, 14);
        cola.insert(20, 20);
        cola.insert(30, 30);
        cola.insert(40, 40);
        cola.insert(50, 50);
        cola.insert(60, 60);
        cola.insert(70, 70);
        System.out.println(cola);
        System.out.println(cola.search(9, COLAStabbingQueryStrategy::topDown));
        System.out.println(cola.searchRange(5, 6, COLARangeQueryStrategy::fractionalCascade));
        System.out.println(cola.searchRange(5, 6, COLARangeQueryStrategy::topDown));
        System.out.println();
        System.out.println(cola.searchRange(3, 7, COLARangeQueryStrategy::fractionalCascade));
        System.out.println(cola.searchRange(3, 7, COLARangeQueryStrategy::topDown));
        System.out.println();
        System.out.println(cola.searchRange(2, 8, COLARangeQueryStrategy::fractionalCascade));
        System.out.println(cola.searchRange(2, 8, COLARangeQueryStrategy::topDown));
        System.out.println();
        System.out.println(cola.searchRange(2, 6, COLARangeQueryStrategy::fractionalCascade));
        System.out.println(cola.searchRange(2, 6, COLARangeQueryStrategy::topDown));
        System.out.println();
        System.out.println(cola.searchRange(8, 8, COLARangeQueryStrategy::fractionalCascade));
        System.out.println(cola.searchRange(8, 8, COLARangeQueryStrategy::topDown));
        System.out.println();
        System.out.println(cola.searchRange(12, 12, COLARangeQueryStrategy::fractionalCascade));
        System.out.println(cola.searchRange(12, 12, COLARangeQueryStrategy::topDown));
        System.out.println();
        System.out.println(cola.searchRange(7, 9, COLARangeQueryStrategy::fractionalCascade));
        System.out.println(cola.searchRange(7, 9, COLARangeQueryStrategy::topDown));
        System.out.println();
        System.out.println(cola.searchRange(13, 13, COLARangeQueryStrategy::fractionalCascade));
        System.out.println(cola.searchRange(13, 13, COLARangeQueryStrategy::topDown));
        System.out.println();
        System.out.println(cola.searchRange(1, 2, COLARangeQueryStrategy::fractionalCascade));
        System.out.println(cola.searchRange(1, 2, COLARangeQueryStrategy::topDown));
    }
}
