package randoms;

import utils.csv.BufferedCsvReader;

public class TestDS {

    public static void main(String[] args) {
        BufferedCsvReader reader = new BufferedCsvReader("D:/Data Sets/0165113-230224095556074/0165113-230224095556074.csv");
        System.out.println(reader.stream().limit(100).mapToInt(line -> line.getLine().getBytes().length).average().orElseThrow());
        reader.close();
    }

    // 575.97 bytes * 10_000_000
}
