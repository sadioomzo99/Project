package container;

import core.BaseSequence;
import datastructures.container.Container;
import datastructures.container.impl.SizedDNAContainer;
import datastructures.container.translation.DNAAddrManager;
import datastructures.container.utils.DNAContainerUtils;
import utils.AddressedDNA;
import utils.lsh.minhash.MinHashLSH;
import utils.lsh.storage.LSHStorage;
import java.util.Arrays;
import java.util.List;

public class ContainerTest {

    public static void main(String[] args) {
        DNAAddrManager atm = DNAAddrManager
                .builder()
                .setLsh(MinHashLSH.newSeqAmpLSHTraditional(5, 200, 20, LSHStorage.AmplifiedLSHStorage.Amplification.OR))
                .setAddrSize(4 * 20)
                //.setNumPermutations(4)
                .build();

        SizedDNAContainer container = SizedDNAContainer
                .builder()
                .setAddressManager(atm)
                .setPayloadSize(4 * 30)
                //.setNumPayloadPermutations(4)
                .build();
        long emptyListId = DNAContainerUtils.putEmptyList(container);
        System.out.println("empty list:");
        System.out.println(DNAContainerUtils.getList(container, emptyListId));
        var s1 = new BaseSequence("TTTT");
        var s2 = new BaseSequence("GGGG");
        var s3 = new BaseSequence("CCCC");
        DNAContainerUtils.appendToList(container, emptyListId, List.of(s1));
        DNAContainerUtils.appendToList(container, emptyListId, List.of(s2, s3, s3));
        DNAContainerUtils.appendToList(container, emptyListId, List.of(s1));

        System.out.println("after appendToList operation(s):");
        System.out.println(DNAContainerUtils.getList(container, emptyListId));

        System.out.println("it:");
        DNAContainerUtils.getListIterator(container, emptyListId).forEachRemaining(System.out::println);
        Arrays.stream(container.getAddresses(emptyListId)).map(BaseSequence::length).forEach(System.out::println);
        Arrays.stream(container.getOligos(emptyListId)).map(AddressedDNA::address).mapToInt(BaseSequence::length).forEach(System.out::println);
        Arrays.stream(container.getOligos(emptyListId)).map(AddressedDNA::payload).mapToInt(BaseSequence::length).forEach(System.out::println);
        System.out.println("bad addresses: " + atm.badAddressesCount());
    }
}
