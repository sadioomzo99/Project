package randoms.paperstuff;

import utils.csv.BufferedCsvReader;
import utils.csv.BufferedCsvWriter;
import java.util.stream.Stream;

public class Figure1 {
    public static void main(String[] args) {
        BufferedCsvReader reader = new BufferedCsvReader("D:/Data Sets/0165113-230224095556074/detailed.csv");
        BufferedCsvWriter writer = new BufferedCsvWriter("D:/Data Sets/0165113-230224095556074/detailed_cleaned.csv");

        writer.appendNewLine(Stream.of("path", "coder", "count", "addrSize", "permutations", "payloadSize", "payloadPadding", "lshType", "k", "r", "b", "nBits", "GC", "HP", "type").toArray(String[]::new));
        // "addrGC", "addrHP", "oligoGC", "oligoHP"
        // "path", "coder" "count", "addrSize", "addrPermutations", "payloadSize", "payloadPadding", "payloadPermutations", "lshType", "k", "r", "b", "nBits", "addrGC", "addrHP", "oligoGC", "oligoHP", "payloadGC", "payloadHP"
        reader.forEach(line -> {
            writer.appendNewLine(
                    Stream.concat(Stream.of("path", "coder", "count", "addrSize", "addrPermutations", "payloadSize", "payloadPadding", "lshType", "k", "r", "b", "nBits", "addrGC", "addrHP")
                                    .map(s -> {
                                        if (s.equals("coder")) {
                                            var v = line.get(s);
                                            if (v == null || v.isEmpty())
                                                return "Fountain Code";
                                        }
                                        return line.get(s);
                                    }),
                            Stream.of("Address")).toArray(String[]::new)
            );
            writer.appendNewLine(
                    Stream.concat(Stream.of("path", "coder", "count", "addrSize", "payloadPermutations", "payloadSize", "payloadPadding", "lshType", "k", "r", "b", "nBits", "oligoGC", "oligoHP")
                                    .map(s -> {
                                        if (s.equals("coder")) {
                                            var v = line.get(s);
                                            if (v == null || v.isEmpty())
                                                return "Fountain Code";
                                        }
                                        return line.get(s);
                                    }),
                            Stream.of("Oligo")).toArray(String[]::new)
            );
            writer.appendNewLine(
                    Stream.concat(Stream.of("path", "coder", "count", "addrSize", "payloadPermutations", "payloadSize", "payloadPadding", "lshType", "k", "r", "b", "nBits", "payloadGC", "payloadHP")
                                    .map(s -> {
                                        if (s.equals("coder")) {
                                            var v = line.get(s);
                                            if (v == null || v.isEmpty())
                                                return "Fountain Code";
                                        }
                                        return line.get(s);
                                    }),
                            Stream.of("Payload")).toArray(String[]::new)
            );
        });

        writer.close();
        reader.close();
    }
}
