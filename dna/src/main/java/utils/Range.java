package utils;

public class Range<T extends Comparable<T>> extends Pair<T, T> implements Cloneable {
    public Range(T t1, T t2) {
        super(t1, t2);
    }

    public void extend(T t) {
        if (super.t1.compareTo(t) > 0)
            super.t1 = t;
        else if (super.t2.compareTo(t) < 0)
            super.t2 = t;
    }

    public void extend(Range<T> r) {
        extend(r.t1);
        extend(r.t2);
    }


    public boolean intersects(Range<T> range) {
        return intersects(range.t1, range.t2);
    }
    public boolean intersects(T t1, T t2) {
        return intersects(t1, t1, super.t2) && intersects(t2, super.t1, t2);
    }
    public boolean intersects(T t) {
        return intersects(t, t1, t2);
    }
    public static <T extends Comparable<T>> boolean intersects(T t, T min, T max) {
        return min.compareTo(t) <= 0 && max.compareTo(t) >= 0;
    }

    public boolean isEmpty() {
        return t1.compareTo(t2) == 0;
    }

    @Override
    public Range<T> clone() {
        return new Range<>(t1, t2);
    }

    public static class NumberRange<T extends Number & Comparable<T>> extends Range<T> {
        public NumberRange(T t1, T t2) {
            super(t1, t2);
            if (t1.compareTo(t2) > 0)
                throw new RuntimeException("range not valid: for a > b the range [a, b] is valid while [b, a] is not");
        }

        public NumberRange(T t) {
            this(t, t);
        }
        public T min() {
            return t1;
        }
        public T max() {
            return t2;
        }
        public Number average() {
            return (t2.doubleValue() + t1.doubleValue()) / 2d;
        }
        @Override
        public Range.NumberRange<T> clone() {
            return new Range.NumberRange<>(t1, t2);
        }
    }
}

