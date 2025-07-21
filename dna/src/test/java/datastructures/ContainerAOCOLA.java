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

public class ContainerAOCOLA {

    public static void main(String[] args) {
        RunBuilderStrategy<Integer, Integer> mem = RunBuilderStrategy.memory();
        COLA<Integer, Integer> cola = new BasicCOLA<>(new BasicRunsManager<>(mem));
        cola.insert(1, 1);
        cola.insert(2, 2);
        cola.insert(4, 4);
        cola.insert(5, 5);

        System.out.println("initial COLA state:");
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

        long colaId = CCOLAUtils.putAOCOLA(container, cola, 0, entryCoder);
        System.out.println("\ndecoding from containter:");
        CCOLAUtils.getAOCOLA(container, colaId, mem, entryCoder).forEach(System.out::println);


        var runBuilder0 = mem.getRunBuilder(0);
        runBuilder0.add(new KVEntry<>(40, 40));
        var run0 = runBuilder0.build();

        CCOLAUtils.appendAOCOLARun(container, colaId, run0, entryCoder);
        System.out.println("\nafter appending: " + run0);
        CCOLAUtils.getAOCOLA(container, colaId, mem, entryCoder).forEach(System.out::println);

        int level = 2;
        System.out.println("\nrun at level: " + level);
        System.out.println(CCOLAUtils.getAOCOLARun(container, colaId, level, entryCoder));

        int pos = 1;
        System.out.println("\nrun level: " + level + ", pos: " + pos + " -> " + CCOLAUtils.getAOCOLAEntry(container, colaId, level, pos, entryCoder));

        int key = 1;
        System.out.println("\nsearching the key: " + key);
        System.out.println(CCOLAUtils.searchStabbingAOCOLA(container, colaId, key, entryCoder));


        int keyLow = 3;
        int keyHigh = 10;
        System.out.println("\nrange query on: [" + keyLow + ", " + keyHigh + "]");
        CCOLAUtils.searchRangeAOCOLA(container, colaId, keyLow, keyHigh, entryCoder).forEach(System.out::println);
        System.out.println("\nby stream patches:");
        CCOLAUtils.searchRangeAOCOLA(container, colaId, keyLow, keyHigh, entryCoder).streamPatches().flatMap(RangeSearchResult.ResultPatch::stream).forEach(System.out::println);
    }
}
