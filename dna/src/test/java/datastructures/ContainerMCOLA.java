package datastructures;

import core.BaseSequence;
import datastructures.cola.BasicCOLA;
import datastructures.cola.COLA;
import datastructures.cola.queryresult.RangeSearchResult;
import datastructures.cola.run.managers.BasicRunsManager;
import datastructures.cola.runbuilderstrategy.RunBuilderStrategy;
import datastructures.container.DNAContainer;
import datastructures.container.ccola.CCOLAUtils;
import datastructures.container.impl.SizedDNAContainer;
import datastructures.container.translation.DNAAddrManager;
import dnacoders.dnaconvertors.RotatingQuattro;
import utils.Coder;
import utils.lsh.minhash.MinHashLSH;
import utils.lsh.storage.LSHStorage;

public class ContainerMCOLA {

    public static void main(String[] args) {
        RunBuilderStrategy<Integer, Integer> mem = RunBuilderStrategy.memory();
        COLA<Integer, Integer> cola = new BasicCOLA<>(new BasicRunsManager<>(mem));
        cola.insert(1, 1);
        cola.insert(2, 2);
        cola.insert(3, 3);
        cola.insert(4, 4);
        cola.insert(5, 5);
        System.out.println(cola);
        DNAAddrManager atm = DNAAddrManager
                .builder()
                .setLsh(MinHashLSH.newSeqAmpLSHTraditional(5, 200, 20, LSHStorage.AmplifiedLSHStorage.Amplification.OR))
                .setAddrSize(80)
                .build();
        DNAContainer container = SizedDNAContainer.builder().setAddressManager(atm).setPayloadSize(150).build();


        Coder<KVEntry<Integer, Integer>, BaseSequence> entryCoder = Coder.fuse(
                Coder.of(e -> e.key() + ";" + e.value(), (String s) -> {
                    var split = s.split(";");
                    return new KVEntry<>(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                }),
                RotatingQuattro.INSTANCE
        );


        var runBuilder0 = mem.getRunBuilder(0);
        runBuilder0.add(new KVEntry<>(40, 40));
        var run0 = runBuilder0.build();

        var runBuilder1 = mem.getRunBuilder(1);
        runBuilder1.add(new KVEntry<>(50, 50));
        runBuilder1.add(new KVEntry<>(55, 55));
        var run1 = runBuilder1.build();


        var runBuilder2 = mem.getRunBuilder(3);
        runBuilder2.add(new KVEntry<>(-1, -1));
        var run2 = runBuilder2.build();

        System.out.println("\nafter initially putting COLA:");
        long colaId = CCOLAUtils.putMCOLA(container, cola, 0, entryCoder);
        CCOLAUtils.getMCOLA(container, colaId, mem, entryCoder).forEach(r -> System.out.println("level: " + r.level() + " -> " + r));

        System.out.println("\nafter replacing run at level: " + run0.level() + " with: " + run0);
        CCOLAUtils.replaceMCOLARun(container, colaId, run0, entryCoder);
        CCOLAUtils.getMCOLA(container, colaId, mem, entryCoder).forEach(r -> System.out.println("level: " + r.level() + " -> " + r));

        System.out.println("\nafter replacing run at level: " + run1.level() + " with: " + run1);
        CCOLAUtils.replaceMCOLARun(container, colaId, run1, entryCoder);
        CCOLAUtils.getMCOLA(container, colaId, mem, entryCoder).forEach(r -> System.out.println("level: " + r.level() + " -> " + r));

        System.out.println("\nafter appending the following run to the end: " + run2);
        CCOLAUtils.appendMCOLARun(container, colaId, run2, entryCoder);
        CCOLAUtils.getMCOLA(container, colaId, mem, entryCoder).forEach(r -> System.out.println("level: " + r.level() + " -> " + r));


        int level = 2;
        System.out.println("\nload run(level=" + level + "): " + CCOLAUtils.getMCOLARun(container, colaId, level, entryCoder));

        int pos = 1;
        System.out.println("\nrun level: " + level + ", pos: " + pos + " -> " + CCOLAUtils.getMCOLAEntry(container, colaId, level, pos, entryCoder));

        int key = 55;
        System.out.println("\nsearching the key: " + key);
        System.out.println(CCOLAUtils.searchStabbingMCOLA(container, colaId, key, entryCoder));

        int keyLow = 3;
        int keyHigh = 50;
        System.out.println("\nrange query on: [" + keyLow + ", " + keyHigh + "]");
        CCOLAUtils.searchRangeMCOLA(container, colaId, keyLow, keyHigh, entryCoder).forEach(System.out::println);
        System.out.println("\nby stream patches:");
        CCOLAUtils.searchRangeMCOLA(container, colaId, keyLow, keyHigh, entryCoder).streamPatches().flatMap(RangeSearchResult.ResultPatch::stream).forEach(System.out::println);
    }
}
