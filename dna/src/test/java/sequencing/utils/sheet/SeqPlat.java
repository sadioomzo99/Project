package sequencing.utils.sheet;

import utils.Range;
import java.time.Duration;

public record SeqPlat(
        String platform, // device's name
        Range.NumberRange<Integer> millionReads, // num reads in given config
        Read read, // len + SE/PE
        Duration runTime, // d
        Range.NumberRange<Double> yield, // Gb
        Float rateMax, // Gb/d
        Range.NumberRange<Integer> reagentsMax, // $
        Range.NumberRange<Float> pricePerGbp, // $
        Range.NumberRange<Integer> hg30xMin, // $
        Range.NumberRange<Integer> hg30xMinPlus5YrInstrAmm, // $
        Range.NumberRange<Integer> hg30xMinPlus2_5YrAmm10Serv90Util, // $
        Range.NumberRange<Float> machine, // $
        Integer installBase, // how many devices are installed worldwide
        String availability, // worldwide, etc.
        Float maxTheoreticalOutputPerDay, // Gb
        Float maxTotalOutputPerDayFromCurrentInstallBase, // Gb
        Integer numberOfHuman30XWGSPerYear // number of human genome sequencing per year, given 30x coverage)
        ) {

    public static SeqPlatBuilder builder() {
        return new SeqPlatBuilder();
    }

    public enum ReadConfig {
        SE, PE
    }

    public record Read(Range.NumberRange<Integer> length, ReadConfig config) {

    }
}
