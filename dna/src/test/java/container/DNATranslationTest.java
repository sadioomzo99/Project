package container;

import core.BaseSequence;
import datastructures.container.translation.AddressManager;
import datastructures.container.translation.DNAAddrManager;
import java.util.List;
import java.util.stream.LongStream;

public class DNATranslationTest {

    public static void main(String[] args) {
        DNAAddrManager addrManager = DNAAddrManager.builder().build();
        List<AddressManager.ManagedAddress<Long, BaseSequence>> list = LongStream.range(0L, 10_000L).parallel().mapToObj(addrManager::routeAndTranslate).toList();
        System.out.println("done translation");
        System.out.println(addrManager.size());
        System.out.println(addrManager.addressRoutingManager().size());
        System.out.println(addrManager.addressTranslationManager().size());
        list.stream().parallel().forEach(m -> {
            var routed = addrManager.addressRoutingManager().get(m.original());
            var translated = addrManager.addressTranslationManager().get(m.routed());
            var probe = AddressManager.ManagedAddress.of(routed, translated);
            if (m.routed() == null || routed.routed() == null || !m.routed().equals(routed.routed()))
                throw new RuntimeException("inconsistent! " + m + " -> " + routed + " -> " + translated);
            if (!probe.equals(m)) {
                throw new RuntimeException("m.original(" + m.original() + "; " + m.original().getClass().getSimpleName() + ") == p.original(" + probe.original() + "; " + probe.original().getClass().getSimpleName() + ") -> " + m.original().equals(probe.original()) + "\n" +
                        "m.routed(" + m.routed() + "; " + m.routed().getClass().getSimpleName() + ") == p.routed(" + probe.routed() + "; " + probe.routed().getClass().getSimpleName() + ") -> " + m.routed().equals(probe.routed()) + "\n" +
                        "m.translated(" + m.translated() + "; " + m.translated().getClass().getSimpleName() + ") == p.translated(" + probe.translated() + "; " + probe.translated().getClass().getSimpleName() + ") -> " + m.translated().equals(probe.translated()));
            }

        });

    }
}
