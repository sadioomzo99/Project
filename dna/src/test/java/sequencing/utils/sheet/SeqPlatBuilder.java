package sequencing.utils.sheet;

import utils.Range;
import java.time.Duration;

public class SeqPlatBuilder {
    private String platform;
    private Range.NumberRange<Integer> millionReads;
    private SeqPlat.Read read;
    private Duration runTime;
    private Range.NumberRange<Double> yield;
    private Float rateMax;
    private Range.NumberRange<Integer>  reagentsMax;
    private Range.NumberRange<Float> pricePerGbp;
    private Range.NumberRange<Integer> hg30xMin;
    private Range.NumberRange<Integer> hg30xMinPlus5YrInstrAmm;
    private Range.NumberRange<Integer> hg30xMinPlus25YrAmm10Serv90Util;
    private Range.NumberRange<Float>  machine;
    private Integer installBase;
    private String availability;
    private Float maxTheoreticalOutputPerDay;
    private Float maxTotalOutputPerDayFromCurrentInstallBase;
    private Integer numberOfHuman30XWGSPerYear;

    public SeqPlatBuilder setPlatform(String platform) {
        this.platform = platform;
        return this;
    }

    public SeqPlatBuilder setMillionReads(Range.NumberRange<Integer> millionReads) {
        this.millionReads = millionReads;
        return this;
    }

    public SeqPlatBuilder setRead(SeqPlat.Read read) {
        this.read = read;
        return this;
    }

    public SeqPlatBuilder setRunTime(Duration runTime) {
        this.runTime = runTime;
        return this;
    }

    public SeqPlatBuilder setYield(Range.NumberRange<Double> yield) {
        this.yield = yield;
        return this;
    }

    public SeqPlatBuilder setRateMax(Float rateMax) {
        this.rateMax = rateMax;
        return this;
    }

    public SeqPlatBuilder setReagentsMax(Range.NumberRange<Integer>  reagentsMax) {
        this.reagentsMax = reagentsMax;
        return this;
    }

    public SeqPlatBuilder setPricePerGbp(Range.NumberRange<Float> pricePerGbp) {
        this.pricePerGbp = pricePerGbp;
        return this;
    }

    public SeqPlatBuilder setHg30xMin(Range.NumberRange<Integer>  hg30xMin) {
        this.hg30xMin = hg30xMin;
        return this;
    }

    public SeqPlatBuilder setHg30xMinPlus5YrInstrAmm(Range.NumberRange<Integer>  hg30xMinPlus5YrInstrAmm) {
        this.hg30xMinPlus5YrInstrAmm = hg30xMinPlus5YrInstrAmm;
        return this;
    }

    public SeqPlatBuilder setHg30xMinPlus2_5YrAmm10Serv90Util(Range.NumberRange<Integer>  hg30xMinPlus25YrAmm10Serv90Util) {
        this.hg30xMinPlus25YrAmm10Serv90Util = hg30xMinPlus25YrAmm10Serv90Util;
        return this;
    }

    public SeqPlatBuilder setMachine(Range.NumberRange<Float>  machine) {
        this.machine = machine;
        return this;
    }

    public SeqPlatBuilder setInstallBase(Integer installBase) {
        this.installBase = installBase;
        return this;
    }

    public SeqPlatBuilder setAvailability(String availability) {
        this.availability = availability;
        return this;
    }

    public SeqPlatBuilder setMaxTheoreticalOutputPerDay(Float maxTheoreticalOutputPerDay) {
        this.maxTheoreticalOutputPerDay = maxTheoreticalOutputPerDay;
        return this;
    }

    public SeqPlatBuilder setMaxTotalOutputPerDayFromCurrentInstallBase(Float maxTotalOutputPerDayFromCurrentInstallBase) {
        this.maxTotalOutputPerDayFromCurrentInstallBase = maxTotalOutputPerDayFromCurrentInstallBase;
        return this;
    }

    public SeqPlatBuilder setNumberOfHuman30XWGSPerYear(Integer numberOfHuman30XWGSPerYear) {
        this.numberOfHuman30XWGSPerYear = numberOfHuman30XWGSPerYear;
        return this;
    }

    public SeqPlat build() {
        return new SeqPlat(platform, millionReads, read, runTime, yield, rateMax, reagentsMax, pricePerGbp, hg30xMin, hg30xMinPlus5YrInstrAmm, hg30xMinPlus25YrAmm10Serv90Util, machine, installBase, availability, maxTheoreticalOutputPerDay, maxTotalOutputPerDayFromCurrentInstallBase, numberOfHuman30XWGSPerYear);
    }
}
