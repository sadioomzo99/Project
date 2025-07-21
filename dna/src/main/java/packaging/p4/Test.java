package packaging.p4;

import core.BaseSequence;
import dnacoders.dnaconvertors.RotatingTre;
import utils.Coder;
import utils.DNAPacker;
import utils.csv.BufferedCsvReader;
import utils.csv.CsvLine;
import java.util.*;
import java.util.stream.Collectors;

public class Test {

    public static void main(String[] args) {
        int count = 10_000;
        BufferedCsvReader reader = new BufferedCsvReader("../gfbio.csv");
        List<CsvLine> list = reader.stream().limit(count).toList();
        reader.close();

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
        Coder<List<CsvLine>, BaseSequence> valuesCoder = new Coder<>() {
            Map<String, Integer> colMapping;
            @Override
            public BaseSequence encode(List<CsvLine> csvLines) {
                colMapping = csvLines.get(0).getColMapping();

                return RotatingTre.INSTANCE.encode(csvLines.stream().map(CsvLine::getLine).collect(Collectors.joining("\n")));
            }

            @Override
            public List<CsvLine> decode(BaseSequence seq) {
                return Arrays.stream(RotatingTre.INSTANCE.decode(seq).split("\n")).map(line -> new CsvLine(line, colMapping)).toList();
            }
        };

        long t1 = System.currentTimeMillis();
        list.stream().parallel().forEach(l -> valuesCoder.encode(Collections.singletonList(l)));
        System.out.println("time (s) =" + (System.currentTimeMillis() - t1) / 1000f);
    }
}
