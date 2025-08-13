package datastructures.lightweight;

import utils.Range;

public interface LightWeightIndex<IN extends Comparable<IN>> {
    boolean simplePointQuery(IN i);
    boolean simpleRangeQuery(Range<IN> range);
    default boolean simpleRangeQuery(IN p1, IN p2) {
        return simpleRangeQuery(new Range<>(p1, p2));
    }
}
