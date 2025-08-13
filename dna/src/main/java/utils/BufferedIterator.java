package utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface BufferedIterator<E> extends Iterator<E> {
    E peek();
    void advance();

    static <E> BufferedIterator<E> of(Iterator<E> it, int numBuffedElements) {
        return new BufferedIterator<>() {

            final List<E> buff = Stream.generate(() -> (E) null).limit(numBuffedElements).collect(Collectors.toCollection(ArrayList::new));
            int cursor;
            int currentSize;
            E peeked;
            boolean isPeeked;

            public E peek() {
                if (isPeeked)
                    return peeked;

                peeked = next();
                isPeeked = true;
                cursor--;
                return peeked;
            }

            @Override
            public boolean hasNext() {
                return cursor < currentSize || it.hasNext();
            }

            @Override
            public E next() {
                if (isPeeked) {
                    isPeeked = false;
                    cursor++;
                    return peeked;
                }
                if (cursor < currentSize)
                    return buff.get(cursor++);

                rebuildBuffer();
                if (currentSize <= 0)
                    throw new NoSuchElementException();

                return buff.get(cursor++);
            }

            private void rebuildBuffer() {
                currentSize = 0;
                cursor = 0;
                while(it.hasNext() && currentSize < numBuffedElements)
                    buff.set(currentSize++, it.next());
            }

            public void advance() {
                next();
            }
        };
    }

    static <E> BufferedIterator<E> skipPeriodically(BufferedIterator<E> it, int skip) {
        return new BufferedIterator<>() {
            E peeked;
            boolean isPeeked = false;
            boolean isForwarded = false;

            @Override
            public boolean hasNext() {
                if (!isForwarded) {
                    if (!fastForward())
                        return false;
                    isForwarded = true;
                }

                return it.hasNext();
            }

            @Override
            public E peek() {
                if (isPeeked)
                    return peeked;

                if (!isForwarded)
                    fastForward();

                peeked = next();
                isPeeked = true;
                return peeked;
            }

            @Override
            public void advance() {
                next();
            }

            @Override
            public E next() {
                if (isPeeked) {
                    isPeeked = false;
                    return peeked;
                }

                if (!isForwarded)
                    fastForward();

                isForwarded = false;
                return it.next();
            }

            private boolean fastForward() {
                int c = skip;
                while(it.hasNext() && c > 0) {
                    it.next();
                    c--;
                }

                return c == 0;
            }
        };
    }
}
