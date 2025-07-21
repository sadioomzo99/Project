package utils.sequencing;

import utils.Range;

public record SimpleSequencingPlatform(String name, Generation generation, Range.NumberRange<Integer> readLength,
                                       Range.NumberRange<Double> costPerGigaBase, Range.NumberRange<Float> readAccuracy) {

    public boolean isCompatible(int sequenceLength) {
        return this.readLength.intersects(sequenceLength);
    }

    public Range.NumberRange<Double> sequence(double gigaBases) {
        return sequence(gigaBases, 1);
    }


    public Range.NumberRange<Double> sequence(double gigaBases, int coverage) {
        return new Range.NumberRange<>(
                coverage * costPerGigaBase.min() * gigaBases,
                coverage * costPerGigaBase.max() * gigaBases
        );
    }

    public SimpleSequencingPlatform scale(double factor) {
        return new SimpleSequencingPlatform(
                name + "_" + factor,
                generation,
                readLength,
                new Range.NumberRange<>(
                        costPerGigaBase.min() * factor,
                        costPerGigaBase.max() * factor
                ),
                readAccuracy

        );
    }
}
