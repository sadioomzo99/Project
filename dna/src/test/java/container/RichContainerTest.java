package container;

import datastructures.container.DNAContainer;
import datastructures.container.impl.RichDNAContainer;
import dnacoders.dnaconvertors.RotatingQuattro;
import java.util.Arrays;
import java.util.List;

public class RichContainerTest {

    public static void main(String[] args) {
        RichDNAContainer<String> container = DNAContainer.builder().buildToRichContainer(RotatingQuattro.INSTANCE);

        var ref = container.putReference("hello world");
        System.out.println(ref.decode());
        System.out.println(RotatingQuattro.INSTANCE.decode(container.get(ref.sketch().id())));

        var list = container.putList(List.of("item 1", "item 2", "item 3!!!!!!!!!!!!!!"));
        System.out.println(list.size());
        System.out.println(list.get(0));
        System.out.println(list.get(1));
        System.out.println(list.get(2));
        System.out.println(list.decode());

        System.out.println();

        System.out.println(container.getList(list.sketch()));
        list.insert(0, "item 0 !");
        System.out.println(list.decode());


        System.out.println();
        var array = container.putArray(new String[] {"e1", "e2"});
        System.out.println(array.length());
        System.out.println(array.get(0));
        System.out.println(array.stream().toList());
        System.out.println(Arrays.toString(array.oligos()));
        System.out.println(array.get(1));
        System.out.println(container.getArray(array.sketch().id()).get(0));
        System.out.println(array);
    }
}
