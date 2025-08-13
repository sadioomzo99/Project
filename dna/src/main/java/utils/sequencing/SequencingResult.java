package utils.sequencing;

import java.util.List;

public record SequencingResult(List<SequencingRunResult> sequencingRunResults) {

    public double nucleotidesPerSec() {
        return sequencingRunResults.stream().mapToDouble(SequencingRunResult::nucleotidesPerSec).average().orElse(Double.NaN);
    }

    public double cost() {
        return sequencingRunResults.stream().mapToDouble(SequencingRunResult::cost).sum();
    }

    @Override
    public String toString() {
        return "SequencingResult{" +
                "\n\ttotal cost: " + cost() + " $" +
                "\n\t" + sequencingRunResults +
                "\n}";
    }
}
