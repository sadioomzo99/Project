package datastructures.lightweight;

import core.BaseSequence;
import datastructures.container.ccola.HCOLAHeaders;
import datastructures.lightweight.index.ColumnImprint;
import datastructures.lightweight.index.PSMA;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class PSMAContainer {
    public static void main(String[] args) {
        //DNAContainer container = SizedDNAContainer.builder().build();
        Random rand = new Random();
        long min = 400;
        long max = 10000;
        int count = 10;
        List<Long> sortedData = Stream.generate(() -> rand.nextLong(min, max)).limit(count).sorted().distinct().toList();
        PSMA psma = new PSMA(sortedData);
        ColumnImprint ci = new ColumnImprint(40, sortedData.stream().mapToDouble(__ -> __).boxed().toList());
        System.out.println(psma);
        System.out.println(ci);


        System.out.println();
        HCOLAHeaders.COLAHeader<PSMA> h1 = new HCOLAHeaders.COLAHeader<>(0, 1, psma);
        BaseSequence seq = HCOLAHeaders.PSMA_COLA_HEADER_CODER.encode(h1);
        System.out.println(HCOLAHeaders.PSMA_COLA_HEADER_CODER.decode(seq));

        HCOLAHeaders.COLAHeader<ColumnImprint> h2 = new HCOLAHeaders.COLAHeader<>(0, 1, ci);
        seq = HCOLAHeaders.CI_COLA_HEADER_CODER.encode(h2);
        System.out.println(HCOLAHeaders.CI_COLA_HEADER_CODER.decode(seq));
    }
}
