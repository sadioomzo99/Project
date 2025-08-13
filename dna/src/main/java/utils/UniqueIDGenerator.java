package utils;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class UniqueIDGenerator implements Supplier<Long> {

    private long nextFreeId;
    private final long start;
    private Lock lock;

    public UniqueIDGenerator() {
        this(0L);
    }
    public UniqueIDGenerator(long start) {
        this.nextFreeId = start;
        this.start = start;
        this.lock = new ReentrantLock();
    }

    @Override
    public Long get() {
        return getN(1)[0];
    }

    public void advance() {
        get();
    }

    public void sync(long lastId) {
        lock.lock();
        nextFreeId = Math.max(lastId + 1, nextFreeId);
        lock.unlock();
    }

    public long[] getN(int n) {
        lock.lock();
        long start = nextFreeId;
        nextFreeId += n;
        long[] result = new long[n];
        for (int i = 0; i < n; i++)
            result[i] = start++;

        lock.unlock();
        return result;
    }

    public long getStart() {
        return start;
    }

    public synchronized long getCurrentNextFreeId() {
        return nextFreeId;
    }
}
