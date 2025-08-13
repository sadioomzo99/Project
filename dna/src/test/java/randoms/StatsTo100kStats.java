package randoms;

import utils.csv.BufferedCsvReader;
import utils.csv.BufferedCsvWriter;
import java.util.stream.IntStream;

public class StatsTo100kStats {
    public static void main(String[] args) {
        int count = 10_000_000;
        String name = "10000k";
        BufferedCsvReader reader = new BufferedCsvReader("D:/Data Sets/A321_valid.csv", ",");
        BufferedCsvWriter writer = new BufferedCsvWriter("D:/Data Sets/A321_valid_" + name + "_cropped_full.csv", ",");
        writer.appendNewLine("pk" + writer.getDelim() + reader.getHeaderLine());
        IntStream.rangeClosed(1, count).takeWhile(i -> reader.available()).forEach(i -> writer.appendNewLine(i + writer.getDelim() + reader.readLine().toString()));
        writer.close();
    }
}
