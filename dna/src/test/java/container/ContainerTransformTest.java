package container;

import core.BaseSequence;
import datastructures.container.Container;
import datastructures.container.impl.SizedDNAContainer;
import dnacoders.dnaconvertors.RotatingQuattro;
import utils.Coder;
import utils.rq.RQCoder;
import java.nio.charset.StandardCharsets;

public class ContainerTransformTest {

    public static void main(String[] args) {
        container();
    }


    static void container() {
        Coder<byte[], BaseSequence> c1 = RQCoder.DEFAULT_RQ;
        Coder<byte[], BaseSequence> c2 = Coder.fuse(
                Coder.of(bs -> new String(bs, StandardCharsets.UTF_8), s -> s.getBytes(StandardCharsets.UTF_8)),
                RotatingQuattro.INSTANCE
        );
        SizedDNAContainer zContainer = SizedDNAContainer.builder().build();
        Container<Long, byte[]> container = Container.transform(zContainer, c2);
        String s = "hello world, this a a big file ".repeat(100);
        long id = zContainer.registerId();
        container.put(id, s.getBytes(StandardCharsets.UTF_8));
        System.out.println(new String(container.get(id), StandardCharsets.UTF_8));
    }
}
