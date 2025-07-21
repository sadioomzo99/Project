package sequencing.utils;

import sequencing.utils.sheet.SeqPlat;
import utils.FuncUtils;
import utils.Range;
import utils.csv.CsvLine;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class SheetParser {

    public static List<SeqPlat> parseSheet() {

        List<String[]> list = FuncUtils.safeCall(() -> Files.readAllLines(Path.of("D:/Promotion/paper_4/sheet.csv")).stream().skip(1).map(line -> line.split(",")).toList());

        List<SeqPlat> plats = new ArrayList<>();
        for (String[] line : list) {
            plats.add(
                    SeqPlat.builder()
                            .setPlatform(line[0])
                            .setMillionReads(parseMillionReads(line[1]))
                            .setRead(parseRead(line[2]))
                            .setRunTime(parseRunTime(line[3]))
                            .setYield(parseDoubleRange(line[4], line[5]))
                            .setRateMax(parseRateMax(line[6]))
                            .setReagentsMax(parseIntegerRange(line[7]))
                            .setPricePerGbp(parseFloatRange(line[8], line[9]))
                            .setHg30xMin(parseIntegerRange(line[10]))
                            .setHg30xMinPlus5YrInstrAmm(parseIntegerRange(line[11]))
                            .setHg30xMinPlus2_5YrAmm10Serv90Util(parseIntegerRange(line[12]))
                            .setMachine(parseMachineCost(line[13]))
                            .setInstallBase(FuncUtils.tryOrElse(() -> Integer.parseInt(line[14]), () -> null))
                            .setAvailability(line[15])
                            .setMaxTheoreticalOutputPerDay(FuncUtils.tryOrElse(() -> Float.parseFloat(line[15]), () -> null))
                            .setMaxTotalOutputPerDayFromCurrentInstallBase(FuncUtils.tryOrElse(() -> Float.parseFloat(line[16]), () -> null))
                            .setNumberOfHuman30XWGSPerYear(FuncUtils.tryOrElse(() -> Integer.parseInt(line[16]), () -> null))
                            .build()
            );
        }

        return plats;
    }


    static Integer parseHg30xPerYear(CsvLine line) {
        return FuncUtils.tryOrElse(() -> Integer.parseInt(line.get("Number of Human 30X WGS per year")), () -> null);
    }

    static Float parseMaxTheoreticalOutputPerDayFromAllInstallBase(CsvLine line) {
        return FuncUtils.tryOrElse(() -> Float.parseFloat(line.get("Max total output per day (Gb) from current installbase")), () -> null);
    }

    static Float parseMaxTheoreticalOutputPerDay(CsvLine line) {
        return FuncUtils.tryOrElse(() -> Float.parseFloat(line.get("Max theoretical output per day (Gb)")), () -> null);
    }

    static String parseIAvailability(CsvLine line) {
        return line.get("Availability");
    }

    static Integer parseInstallBase(CsvLine line) {
        return FuncUtils.tryOrElse(() -> Integer.parseInt(line.get("Install base")), () -> null);
    }

    static Range.NumberRange<Float> parseMachineCost(CsvLine line) {
        return parseMachineCost(line.get("Machine: ($ K)"));
    }

    static Range.NumberRange<Float> parseMachineCost(String value) {
        var cost = parseFloatRange(value);
        return cost != null ? new Range.NumberRange<>(cost.min() * 1000, cost.max() * 1000) : null;
    }

    static Range.NumberRange<Integer> parseHg30xPriceMinP2xyrsAmm10Serv90Util(CsvLine line) {
        return parseIntegerRange(line.get("hg-30x min plus 2.5yr amm 10% serv 90% util ($)"));
    }

    static Range.NumberRange<Integer> parseHg30xPriceMinP5yrsInstrAmm(CsvLine line) {
        return parseIntegerRange(line.get("hg-30x min plus 5yr instr amm ($)"));
    }

    static Range.NumberRange<Integer> parseHg30xPriceMin(CsvLine line) {
        return parseIntegerRange(line.get("hg-30x min: ($)"));
    }

    static Range.NumberRange<Integer> parseReagentsCost(CsvLine line) {
        return parseIntegerRange(line.get("Reagents max: ($)"));
    }

    static Range.NumberRange<Integer> parseIntegerRange(String value) {
        if (value == null)
            return null;

        if (value.contains("-")) {
            var split = value.split("-");
            var v1 = Integer.parseInt(split[0]);
            var v2 = Integer.parseInt(split[1]);
            return new Range.NumberRange<>(Math.min(v1, v2), Math.max(v1, v2));
        }

        var cost = FuncUtils.tryOrElse(() -> Integer.parseInt(value), () -> null);
        return cost == null ? null : new Range.NumberRange<>(cost, cost);
    }

    static Range.NumberRange<Float> parsePricePerGbs(CsvLine line) {
        return parseFloatRange(line.get("Price per Gbp min: ($)"), line.get("Price per Gbp max: ($)"));
    }

    static Float parseRateMax(CsvLine line) {
        return parseRateMax(line.get("Rate max: (Gb/d)"));
    }

    static Float parseRateMax(String value) {
        return FuncUtils.tryOrElse(() -> Float.parseFloat(value), () -> null);
    }

    static Range.NumberRange<Float> parseYields(CsvLine line) {
        return parseFloatRange(line.get("Yield low: (Gb)"), line.get("Yield high: (Gb)"));
    }

    static Range.NumberRange<Float> parseFloatRange(String... values) {
        var d = parseDoubleRange(values);
        return d == null ? null : new Range.NumberRange<>(d.min().floatValue(), d.max().floatValue());
    }

    static Range.NumberRange<Double> parseDoubleRange(String... values) {
        if (values == null || values.length == 0)
            return null;

        Double low = FuncUtils.tryOrElse(() -> Double.parseDouble(values[0]), () -> null);
        Double high = FuncUtils.tryOrElse(() -> Double.parseDouble(values[1]), () -> null);
        if (low == null && high != null)
            low = high;
        if (high == null && low != null)
            high = low;

        return low == null ? null : new Range.NumberRange<>(Math.min(low, high), Math.max(low, high));
    }

    static Range.NumberRange<Integer> parseMillionReads(CsvLine line) {
        return parseMillionReads(line.get("Reads x run max: (M)"));
    }

    static Range.NumberRange<Integer> parseMillionReads(String value) {
        if (value.contains("-")) {
            var split = value.replaceAll(" ", "").split("-");
            return new Range.NumberRange<>(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
        }
        Integer n = FuncUtils.tryOrElse(() -> Integer.parseInt(value), () -> null);
        return n == null ? null : new Range.NumberRange<>(n, n);
    }

    static Duration parseRunTime(CsvLine line) {
        return parseRunTime(line.get("Run time max: (d)"));
    }

    static Duration parseRunTime(String value) {
        return FuncUtils.tryOrElse(() -> Duration.ofMinutes((long) (Float.parseFloat(value) * 24L * 60L)), () -> null);
    }

    static SeqPlat.Read parseRead(CsvLine line) {
        String e = line.get("Read length max: (paired-end* Half of data in reads**)");
        return parseRead(e);
    }

    static SeqPlat.Read parseRead(String value) {
        SeqPlat.ReadConfig config;
        Range.NumberRange<Integer> length = null;
        if (value.startsWith("PE"))
            config = SeqPlat.ReadConfig.PE;
        else
            config = SeqPlat.ReadConfig.SE;


        if (value.matches("\\d+\\**")) {
            var index = value.indexOf("*");
            length = new Range.NumberRange<>(Integer.parseInt(index == -1 ? value : value.substring(0, index)));
        }
        else if (value.contains("-")) {
            String[] split = value.split("-");
            if (split[1].contains("K"))
                split[0] = split[0] + "000";

            split[1] = split[1].replace("K", "000").replaceAll("\\*", "");
            length = new Range.NumberRange<>(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
        }
        if (value.matches("PE\\d+\\*"))
            length = new Range.NumberRange<>(Integer.parseInt(value.substring(2, value.length() - 1)));
        else if (value.matches("\\d+K\\*+"))
            length = new Range.NumberRange<>(Integer.parseInt(value.replace("K", "000").replaceAll("\\*", "")));

        if (length == null)
            throw new RuntimeException("failed parsing read length for line: " + value);

        return new SeqPlat.Read(length, config);
    }
}
