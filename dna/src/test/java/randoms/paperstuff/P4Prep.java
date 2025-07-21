package randoms.paperstuff;

import utils.Pair;
import utils.csv.BufferedCsvReader;
import utils.csv.BufferedCsvWriter;
import utils.csv.CsvLine;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class P4Prep {

    public static void main(String[] args) {
        BufferedCsvReader reader = new BufferedCsvReader("D:/Promotion/paper_4/results/results_v.csv");
        BufferedCsvWriter writer = new BufferedCsvWriter("D:/Promotion/paper_4/results/results_v_op.csv");

        List<String> attributes = Arrays.asList(
                "addresses",
                "payloads",
                "oligos",
                "internal node oligos",
                "leaf node oligos"
        );

        List<String> aggregates = Arrays.asList(
                "min",
                "max",
                "avg",
                "sd"
        );

        List<String> ofs = Arrays.asList(
                "error",
                "dist",
                "score"
        );

        List<Pair<String, List<String>>> tols = Arrays.asList(
                new Pair<>("leaf nodes", Arrays.asList("tol leaves type", "tol value leaf nodes")),
                new Pair<>("internal nodes", Arrays.asList("tol internal nodes type", "tol value internal nodes"))
        );


        writer.appendNewLine(
                "coder",
                "time in seconds",
                "payload size",
                "payload permutations",
                "num key-value pairs",
                "sketcher",
                "bytes in leaves",
                "total bytes",
                "bit rate",
                "bit rate leaves",
                "b",
                "c",
                "seed trials",
                "error weight",
                "dist weight",
                "num nodes",
                "num oligos",
                "num oligos / node",
                "num oligos of internal nodes",
                "num oligos / internal node",
                "num oligos of leaf nodes",
                "num oligos / leaf",

                "agg kind",
                "aggregate",
                "agg attribute",
                "agg value",

                "tol of",
                "tol type",
                "tol value"
        );

        reader.forEach(line -> tols
                .forEach(tol -> attributes
                        .forEach(att -> aggregates
                                .forEach(agg -> ofs
                                        .forEach(of ->
                                                writer.appendNewLine(
                                                        Stream.of(
                                                                computeCommonInfo(line),
                                                                computeAgg(att, agg, of, line),
                                                                computeTol(tol, line)
                                                        ).flatMap(__ -> __).toArray(String[]::new)
                                                )
                                        )
                                )
                        )
                )
        );

        reader.close();
        writer.close();
    }


    static Stream<String> computeCommonInfo(CsvLine line) {
        return Stream.of(
                "coder",
                "time in seconds",
                "payload size",
                "payload permutations",
                "num key-value pairs",
                "sketcher",
                "bytes in leaves",
                "total bytes",
                "bit rate",
                "bit rate leaves",
                "b",
                "c",
                "seed trials",
                "error weight",
                "dist weight",
                "num nodes",
                "num oligos",
                "num oligos / node",
                "num oligos of internal nodes",
                "num oligos / internal node",
                "num oligos of leaf nodes",
                "num oligos / leaf"
        ).map(line::get);
    }

    static Stream<String> computeAgg(String attribute, String aggregate, String of, CsvLine line) {
        return Stream.of(
                of,
                aggregate,
                attribute,
                line.get(aggregate + " " + of + " " + attribute)
        );
    }

    static Stream<String> computeTol(Pair<String, List<String>> tol, CsvLine line) {
        return Stream.of(
                tol.getT1(),
                line.get(tol.getT2().get(0)),
                line.get(tol.getT2().get(1))
        );
    }
}
