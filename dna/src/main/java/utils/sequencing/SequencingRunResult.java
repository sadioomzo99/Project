package utils.sequencing;

import utils.FuncUtils;
import java.time.Duration;

public record SequencingRunResult(
        SequencingPlatform platform,
        RunOption option,
        Duration duration,
        double cost,
        long numReadSequences,
        long nonUtilizedNucleotidesReads,
        int sequenceLength,
        int coverage
) {

    public double nucleotidesPerSec() {
        return (double) (numReadSequences() * sequenceLength) / duration.toSeconds();
    }

    @Override
    public String toString() {
        return "SequencingRunResult{" +
                "\n\tplatform=" + platform.getName() +
                ", \n\toption=" + option +
                ", \n\tduration=" + FuncUtils.asPrettyString(duration) +
                ", \n\tcost=" + cost +
                ", \n\tnumReadSequences=" + numReadSequences +
                ", \n\tnonUtilizedNucleotidesReads=" + nonUtilizedNucleotidesReads +
                ", \n\tsequenceLength=" + sequenceLength +
                ", \n\tcoverage=" + coverage +
                "\n}";
    }
}
