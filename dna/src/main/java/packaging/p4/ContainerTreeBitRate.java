package packaging.p4;

import core.BaseSequence;
import datastructures.container.DNAContainer;
import utils.AddressedDNA;
import utils.Coder;
import utils.DNAPacker;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class ContainerTreeBitRate {

    public static void main(String[] args) {
        var list = IntStream.range(0, Integer.parseInt(args[1])).boxed().toList();

        Coder<List<Integer>, BaseSequence> keysCoder = new Coder<>() {
            @Override
            public BaseSequence encode(List<Integer> sortedList) {

                BaseSequence seq = new BaseSequence();
                int id0 = sortedList.get(0);
                int id;
                int size = sortedList.size();
                DNAPacker.packUnsigned(seq, id0);
                for (int i = 1; i < size; i++) {
                    id = sortedList.get(i);
                    DNAPacker.packUnsigned(seq, id - id0);
                    id0 = id;
                }

                return seq;
            }

            @Override
            public List<Integer> decode(BaseSequence seq) {
                DNAPacker.LengthBase lb = DNAPacker.LengthBase.parsePrefix(seq);
                List<Integer> keys = new ArrayList<>();
                int delta = lb.unpackSingle(seq, false).intValue();
                keys.add(delta);
                seq = seq.window(lb.totalSize());

                while (seq.length() > 0) {
                    lb = DNAPacker.LengthBase.parsePrefix(seq);
                    delta += lb.unpackSingle(seq, false).intValue();
                    keys.add(delta);
                    seq = seq.window(lb.totalSize());
                }

                return keys;
            }
        };


        var container = DNAContainer.builder().setPayloadSize(Integer.parseInt(args[0]) - 80).build();
        var reference = container.put(keysCoder.encode(list));

        var oligos = container.getOligos(reference);
        int nucleotides = Arrays.stream(oligos).mapToInt(AddressedDNA::length).sum();
        double bits = list.size() * Integer.SIZE;

        System.out.println("num nts: " + nucleotides);
        System.out.println("num oligos: " + oligos.length);
        System.out.println("bit rate: " + bits / nucleotides);
    }
}
