package randoms.paperstuff;

import utils.csv.BufferedCsvReader;
import utils.csv.BufferedCsvWriter;
import java.util.Arrays;
import java.util.stream.Stream;

public class BitrateFig {

    public static void main(String[] args) {
        BufferedCsvReader reader = new BufferedCsvReader("D:\\Promotion\\paper_3_extended\\results\\latest\\summary.csv");
        BufferedCsvWriter writer = new BufferedCsvWriter("D:\\Promotion\\paper_3_extended\\results\\latest\\summary_br_cleaned.csv");

        writer.appendNewLine("time in seconds",
                "coder",
                "LSH type",
                "addr_size",
                "addr_permutations",
                "payload_size",
                "payload_padding",
                "payload_permutations",
                "oligo size",
                "num objects inserted",
                "MBs",
                "num oligos",
                "num oligos / object",
                "bit rate",
                "addr in use",
                "bad addrs",

                "min oligos gc",
                "max oligo gc",
                "avg oligo gc",
                "min oligo hp",
                "max oligo hp",
                "avg oligo hp",

                "min Addrs gc",
                "max Addrs gc",
                "avg Addrs gc",
                "min Addrs hp",
                "max Addrs hp",
                "avg Addrs hp",
                "type");

        reader.forEach(line -> {
            writer.appendNewLine(
                    Stream.concat(Arrays.stream(line.get( "time in seconds",
                            "coder",
                            "LSH type",
                            "addr_size",
                            "addr_permutations",
                            "payload_size",
                            "payload_padding",
                            "payload_permutations",
                            "oligo size",
                            "num objects inserted",
                            "MBs",
                            "num oligos",
                            "num oligos / object",
                            "bit rate",
                            "addr in use",
                            "bad addrs",

                            "min oligos gc",
                            "max oligo gc",
                            "avg oligo gc",
                            "min oligo hp",
                            "max oligo hp",
                            "avg oligo hp",

                            "min Addrs gc",
                            "max Addrs gc",
                            "avg Addrs gc",
                            "min Addrs hp",
                            "max Addrs hp",
                            "avg Addrs hp")), Stream.of("Oligo")).toArray(String[]::new)
            );

            writer.appendNewLine(
                    Stream.concat(Arrays.stream(line.get( "time in seconds",
                            "coder",
                            "LSH type",
                            "addr_size",
                            "addr_permutations",
                            "payload_size",
                            "payload_padding",
                            "payload_permutations",
                            "oligo size",
                            "num objects inserted",
                            "MBs",
                            "num oligos",
                            "num oligos / object",
                            "payload bit rate",
                            "addr in use",
                            "bad addrs",

                            "min oligos gc",
                            "max oligo gc",
                            "avg oligo gc",
                            "min oligo hp",
                            "max oligo hp",
                            "avg oligo hp",

                            "min Addrs gc",
                            "max Addrs gc",
                            "avg Addrs gc",
                            "min Addrs hp",
                            "max Addrs hp",
                            "avg Addrs hp")), Stream.of("Payload")).toArray(String[]::new)
            );
        });

        reader.close();
        writer.close();

    }
}
