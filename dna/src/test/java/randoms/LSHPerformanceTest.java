package randoms;

import utils.csv.BufferedCsvWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LSHPerformanceTest {

    public static void main(String[] args) throws Exception {

        BufferedCsvWriter csv = new BufferedCsvWriter("lsh_params_.csv");
        csv.appendNewLine(
                "hashFunctions",
                "bandSize",
                "bands",
                "s",
                "t",
                "s - t",
                "no-band-match",
                "single-band-match",
                "some-band-match",
                "TP",
                "FP",
                "TN",
                "FN"
        );


        for (int hs = 1; hs < 50; hs += 1) {
            for (int r : bandSizes(hs)) {
                int b = hs / r;
                float t = t(r, b);
                for (float s = 0.0f; s <= 1.0f; s += 0.01f) {
                    s = round(s, 2);
                    float singleBandMatches = singleBandMatches(s, r);
                    float someBandMatches = SomeBandMatches(s, r, b);
                    float noBandMatches = noBandMatches(s, r, b);
                    float TN = 0;
                    float FN = 0;
                    float TP = 0;
                    float FP = 0;
                    if (s >= t) {
                        FN = noBandMatches;
                        TP = someBandMatches;

                    }
                    else {
                        TN = noBandMatches;
                        FP = someBandMatches;
                    }
                    csv.appendNewLine(
                            Stream.of(
                                    hs,
                                    r,
                                    b,
                                    s,
                                    t,
                                    s - t,
                                    noBandMatches,
                                    singleBandMatches,
                                    someBandMatches,
                                    TP,
                                    FP,
                                    TN,
                                    FN).map(String::valueOf).toArray(String[]::new)
                    );
                }
            }
        }

        csv.close();
    }


    static List<Integer> bandSizes(int hashTables) {
        List<Integer> result = Stream.iterate(1, x -> x + 1).limit(hashTables / 2).filter(x -> hashTables % x == 0).collect(Collectors.toList());
        result.add(hashTables);
        return result;

    }

    static float singleBandMatches(float s, int r) {
        return (float) Math.pow(s, r);
    }

    static float SingleBandNoMatch(float s, int r) {
        return 1.0f - singleBandMatches(s, r);
    }

    static float noBandMatches(float s, int r, int b) {
        return (float) Math.pow(SingleBandNoMatch(s, r), b);
    }

    static float SomeBandMatches(float s, int r, int b) {
        return 1.0f - noBandMatches(s, r, b);
    }

    static float t(int r, int b) {
        return (float) Math.pow((1.0f / b), 1.0f / r);
    }

    public static float round(Number value, int places) {
        BigDecimal bd = BigDecimal.valueOf(value.doubleValue());
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

}
