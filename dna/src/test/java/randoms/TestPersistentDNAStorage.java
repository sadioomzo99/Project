package randoms;

import core.BaseSequence;
import datastructures.container.DNAContainer;
import utils.FuncUtils;

import java.util.stream.IntStream;

public class TestPersistentDNAStorage {

    public static void main(String[] args) {
        FuncUtils.deleteFile("dnacontainer.store");
        var container = DNAContainer.builder().setStorePersistentDefault().build();
        IntStream.range(0, 10).parallel().forEach(id -> container.put(BaseSequence.random(2)));
        System.out.println(container.values());
    }
}
