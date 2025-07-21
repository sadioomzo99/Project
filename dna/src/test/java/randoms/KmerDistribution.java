package randoms;

import core.Base;
import core.BaseSequence;
import utils.FuncUtils;
import utils.csv.BufferedCsvWriter;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KmerDistribution {

    public static void main(String[] args) {
        int l = 50;
        int k = 5;
        int n = 1000_000;
        BufferedCsvWriter writer = new BufferedCsvWriter("kmers.csv", false);
        writer.appendNewLine(
                "kmer",
                "frequency"
        );
        Stream.generate(() -> Stream.generate(() -> FuncUtils.random(Base.values())).limit(l).collect(BaseSequence.COLLECTOR_BASE)).limit(n).flatMap(s -> s.kmers(k).stream())
                .collect(Collectors.groupingBy(km -> km, Collectors.counting()))
                .forEach((key, value) -> writer.appendNewLine(
                        key.toString(),
                        value.toString()
                ));

        writer.close();
    }
}
