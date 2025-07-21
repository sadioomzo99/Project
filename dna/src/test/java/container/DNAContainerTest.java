package container;

import core.BaseSequence;
import datastructures.container.impl.RichDNAContainer;
import datastructures.container.impl.SizedDNAContainer;
import datastructures.container.translation.DNAAddrManager;
import datastructures.container.types.ContainerArray;
import datastructures.container.types.ContainerList;
import utils.lsh.minhash.MinHashLSH;
import utils.lsh.storage.LSHStorage;
import java.util.Arrays;

public class DNAContainerTest {

    public static void main(String[] args) {
        DNAAddrManager atm = DNAAddrManager
                .builder()
                .setLsh(MinHashLSH.newSeqAmpLSHTraditional(5, 200, 20, LSHStorage.AmplifiedLSHStorage.Amplification.OR))
                .setAddrSize(80)
                .build();
        RichDNAContainer<BaseSequence> container = SizedDNAContainer.builder().setAddressManager(atm).setPayloadSize(150).buildToRichContainer();
        BaseSequence s1 = new BaseSequence("AAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        BaseSequence s2 = new BaseSequence("GGGGGGGGGGGGGGGGGGGGGGGGGGGGG");
        BaseSequence s3 = new BaseSequence("TTTTTTTTTTTTTTTTTTTTTTTTTTTTT");
        long id1 = container.put(s1);
        long id2 = container.put(s2);
        long id3 = container.put(s3);

        System.out.println(container.get(id1).equals(s1));
        System.out.println(container.get(id2).equals(s2));
        System.out.println(container.get(id3).equals(s3));

        System.out.println(container.values().stream().mapToInt(BaseSequence::length).summaryStatistics());


        ContainerArray<BaseSequence> array = container.putArray(new BaseSequence[] {s1, s2});
        System.out.println("oligos: " + Arrays.toString(array.oligos()));
        System.out.println("array:");
        System.out.println(array.get(0));
        System.out.println(array.get(1));
        System.out.println("array iterator");
        array.forEach(System.out::println);

        System.out.println("list:");
        ContainerList<BaseSequence> list = container.putList(Arrays.asList(s1, s2));
        System.out.println(Arrays.toString(list.oligos()));

        list.append(s3);
        list.append(s3);
        System.out.println(list.stream().toList());


        System.out.println("empty list:");
        var emptyList = container.putEmptyList(container.registerId());
        System.out.println(emptyList.decode());
    }
}
