package utils;

import org.json.JSONObject;
import utils.rand.Ranlux;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class FuncUtils {
    @FunctionalInterface
    public interface RunnableAttempt {
        void run() throws Exception;
    }
    @FunctionalInterface
    public interface TriFunction<F1, F2, F3, T> {
        T apply(F1 f1, F2 f2, F3 f3);
    }

    /**
     * Runs the given runnableAttempt and converts all exceptions to RuntimeExceptions.
     * @param runnableAttempt the RunnableAttempt object.
     */
    public static void safeRun(RunnableAttempt runnableAttempt) {
        try {
            runnableAttempt.run();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param callable the Callable object.
     * @return the result of the callable object. Converts all exceptions to RuntimeExceptions.
     */
    public static <T> T safeCall(Callable<T> callable) {
        try {
            return callable.call();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param callable the Callable object.
     * @return the result of the callable object, ignoring all exceptions. Returns null in the case of exceptions.
     */
    public static <T> T superSafeCall(Callable<T> callable) {
        try {
            return callable.call();
        }
        catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Runs the given runnableAttempt and ignores all exceptions.
     * @param runnableAttempt the RunnableAttempt object.
     * @return true if no exception was thrown, and false otherwise.
     */
    public static boolean superSafeRun(RunnableAttempt runnableAttempt) {
        try {
            runnableAttempt.run();
            return true;
        }
        catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Parses text from the beginning of the given string to produce a number. The method may not use the entire text of the given string.
     * @param s the input String.
     * @return the Number for the input String.
     */
    public static Number parseNumber(String s) {
        return safeCall(() -> NumberFormat.getInstance(Locale.ENGLISH).parse(s));
    }

    /**
     * Attempts to run rTry. If an exception was thrown, it is ignored and escape is run instead.
     * @param rTry the RunnableAttempt object that is attempted to run.
     * @param escape the other RunnableAttempt object that is run if rTry threw an exception.
     */
    public static void tryOrElse(RunnableAttempt rTry, RunnableAttempt escape) {
        if (!superSafeRun(rTry))
            safeRun(escape);
    }

    /**
     * Attempts to call and return rCallable. If an exception was thrown, escapeCallable is called and returned instead.
     * @param rCallable the Callable object that is attempted to be called and returned.
     * @param escapeCallable the other Callable object that is called and returned if rCallable threw an exception.
     */
    public static <T> T tryOrElse(Callable<T> rCallable, Callable<T> escapeCallable) {
        try {
            return rCallable.call();
        }
        catch (Exception ignored) {
            return safeCall(escapeCallable);
        }
    }

    /**
     * Shuffles the given int array in-place.
     * @param array the supplied array to shuffle.
     */
    public static void shuffle(int[] array) {
        Ranlux rand = new Ranlux(3, ThreadLocalRandom.current().nextLong());
        int takes = array.length - 1;
        int z;
        for (int i = takes; i > 0; i--) {
            z = rand.choose(0, i);
            int iArray = array[i];
            array[i] = array[z];
            array[z] = iArray;
        }
    }

    /**
     * Shuffles the given list in-place.
     * @param list the supplied list to shuffle.
     */
    public static <T> void shuffle(List<T> list) {
        Ranlux rand = new Ranlux(3, ThreadLocalRandom.current().nextLong());

        int takes = list.size() - 1;
        int z;
        for (int i = takes; i > 0; i--) {
            z = rand.choose(0, i);
            T iT = list.get(i);
            list.set(i, list.get(z));
            list.set(z, iT);
        }
    }

    /**
     * Shuffles the given generic array in-place.
     * @param array the supplied array to shuffle.
     */
    public static <T> void shuffle(T[] array) {
        Ranlux rand = new Ranlux(3, ThreadLocalRandom.current().nextLong());
        int takes = array.length - 1;
        int z;
        for (int i = takes; i > 0; i--) {
            z = rand.choose(0, i);
            T iArray = array[i];
            array[i] = array[z];
            array[z] = iArray;
        }
    }


    /**
     * Executes the given Runnable in a new single pool executor .
     * @param runnable the Runnable to be run asynchronously.
     */
    public static void executeAsync(Runnable runnable) {
        runOnPool(Executors.callable(runnable), Executors.newSingleThreadExecutor());
    }

    /**
     * Calls the given Callable in a new single pool executor and returns its Future handler.
     * @param callable the Callable to be called asynchronously.
     * @return the Future handler for this callable.
     */
    public static <T> Future<T> callAsync(Callable<T> callable) {
        return runOnPool(callable, Executors.newSingleThreadExecutor());
    }

    /**
     * Executes the given Runnable in a new work-stealing pool.
     * @param runnable the Runnable to be run asynchronously.
     */
    public static void executeParallelAsync(Runnable runnable) {
        runOnPool(Executors.callable(runnable), Executors.newWorkStealingPool());
    }

    /**
     * Calls the given Callable in a new work-stealing pool and returns its Future handler.
     * @param callable the Callable to be called asynchronously.
     * @return the Future handler for this callable.
     */
    public static <T> Future<T> callParallelAsync(Callable<T> callable) {
        return runOnPool(callable, Executors.newWorkStealingPool());
    }

    private static <T> Future<T> runOnPool(Callable<T> callable, ExecutorService pool) {
        var handler = pool.submit(callable);
        pool.shutdown();
        return handler;
    }

    /**
     * Randomly picks a double value within the given range's [min, max).
     * @param range the supplied range of doubles.
     * @return the random double that was randomly sampled from [min, max) of the given range.
     */
    public static double random(Range<Double> range) {
        return ThreadLocalRandom.current().nextDouble(range.getT1(), range.getT2());
    }

    /**
     * Returns a Permutation (using a seed) with the Fisher-Yates method that is uniform for the given seed and length of sequence.
     * @param seed the seed for the random numbers' generator.
     * @return the Permutation instance.
     */
    public static Permutation getUniformPermutation(long seed, int length) {
        return getUniformPermutation(Ranlux.MAX_LUXURY_LEVEL, seed, length);
    }

    /**
     * Returns a Permutation (using a seed) with the Fisher-Yates method that is uniform for the given seed and length of sequence.
     * @param seed the seed for the random numbers' generator.
     * @param luxuryLevel the luxury level (1,2,3,4) for RandLux. The luxury level 4 is the highest.
     * @return the Permutation instance.
     */
    public static Permutation getUniformPermutation(int luxuryLevel, long seed, int length) {
        Ranlux rand = new Ranlux(luxuryLevel, seed);
        int takes = length - 1;
        int[] swapIndexes = new int[takes * 2];
        int c = 0;
        for (int i = takes; i > 0; i--) {
            swapIndexes[c++] = i;
            swapIndexes[c++] = rand.choose(0, i);
        }

        return new Permutation(swapIndexes);
    }

    /**
     * BLocks until the given future object is ready.
     * @param f the future object.
     * @param <T> the return type.
     * @return the result in that future object.
     */
    public static <T> T await(Future<T> f) {
        return safeCall(f::get);
    }

    /**
     * Converts an iterator object to a respective stream.
     * @param it the input iterator.
     * @return a stream containing the elements of the given iterator.
     */
    public static <T> Stream<T> stream(Iterable<T> it) {
        return StreamSupport.stream(it.spliterator(), false);
    }

    /**
     * @param s the stream.
     * @param parallel specifies whether this stream should be parallel.
     * @return the same stream that is now parallel or sequential depending on the specified boolean.
     */
    public static <S extends BaseStream<?, S>> S stream(S s, boolean parallel) {
        return parallel? s.parallel() : s.sequential();
    }

    /**
     * Creates a stream that consists of the same elements as the input stream, but numbered from 0 to n-1.
     * @param stream the input stream.
     * @return an enumerated stream.
     */
    public static <T, S extends BaseStream<T, S>> Stream<Pair<Integer, T>> enumerate(S stream) {
        AtomicInteger i = new AtomicInteger(0);
        return StreamSupport.stream(stream.spliterator(), stream.isParallel()).map(e -> new Pair<>(i.getAndIncrement(), e));
    }

    /**
     * Creates a stream of lists or chunks for an input stream, each of the lists is of the same chunk size (besides the last chunk/list that could contain fewer elements).
     * @param stream the input stream.
     * @param chunkSize the chunk size.
     * @return the chunked stream.
     */
    public static <T> Stream<List<T>> chunkProgressive(Stream<T> stream, int chunkSize) {
        return stream(() -> new Iterator<>() {
            final Iterator<T> it = stream.iterator();
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public List<T> next() {
                int c = chunkSize;
                List<T> list = new ArrayList<>(chunkSize);
                while (it.hasNext() && c-- > 0) {
                    list.add(it.next());
                }
                return list;
            }
        });
    }

    /**
     * Creates a stream of lists or chunks for an input stream, each of the lists is of the same chunk size (besides the last chunk/list that could contain up to 2 * chunkSize - 1 elements).
     * @param stream the input stream.
     * @param chunkSize the chunk size.
     * @return the chunked stream.
     */
    public static <T> Stream<List<T>> chunkConservative(Stream<T> stream, int chunkSize) {
        return stream(() -> new Iterator<>() {
            final Iterator<T> it = stream.iterator();
            List<T> buffer = buffer();

            @Override
            public boolean hasNext() {
                return buffer != null && !buffer.isEmpty();
            }

            @Override
            public List<T> next() {
                List<T> lookahead = buffer();
                int lookaheadSize = lookahead.size();
                if (lookaheadSize < chunkSize) {
                    buffer.addAll(lookahead);
                    List<T> r = buffer;
                    buffer = null;
                    return r;
                }
                List<T> r = buffer;
                if (lookaheadSize == 0)
                    buffer = null;
                else
                    buffer = lookahead;
                return r;
            }

            private List<T> buffer() {
                List<T> buff = new ArrayList<>(chunkSize);
                int c = chunkSize;
                while(c-- > 0 && it.hasNext())
                    buff.add(it.next());

                return buff;
            }
        });
    }

    /**
     * Creates a stream of copies for an input stream. The initial input stream is collected to a list and streamed from there.
     * @param stream the input stream.
     * @return the stream of copied input stream.
     */
    public static <T> Stream<Stream<T>> copy(Stream<T> stream) {
        return FuncUtils.stream(() -> new Iterator<>() {
            List<T> list = stream.toList();
            @Override
            public boolean hasNext() {
                if (list == null)
                    list = stream.toList();

                return !list.isEmpty();
            }

            @Override
            public Stream<T> next() {
                return list.stream();
            }
        });
    }

    /**
     * Creates a zipped stream of two input streams. The resulting zipped stream contains pairs in which the first object is from the first stream, and the second object is from the second stream at the same position. The objects are filled up with null if the two streams are not of equal length.
     * @param s1 the first input stream.
     * @param s2 the second input stream.
     * @return the zipped stream.
     */
    public static <T1, T2, S1 extends BaseStream<T1, S1>, S2 extends BaseStream<T2, S2>> Stream<Pair<T1, T2>> zip(S1 s1, S2 s2) {
        return zip(s1, s2, Pair::new);
    }

    /**
     * Creates a zipped stream of two input streams. The resulting zipped stream contains Objects created by the BiFunction<T1, T2, Z> supplied in which the first object is from the first stream, and the second object is from the second stream at the same position. The objects are filled up with null if the two streams are not of equal length.
     * @param s1 the first input stream.
     * @param s2 the second input stream.
     * @param zof the function that zips one object from s1 and another from s2 to a zipped object.
     * @return the zipped stream.
     */
    public static <T1, T2, Z, S1 extends BaseStream<T1, S1>, S2 extends BaseStream<T2, S2>> Stream<Z> zip(S1 s1, S2 s2, BiFunction<T1, T2, Z> zof) {
        return stream(() ->  new Iterator<>() {
            final Iterator<T1> it1 = s1.iterator();
            final Iterator<T2> it2 = s2.iterator();

            @Override
            public boolean hasNext() {
                return it1.hasNext() || it2.hasNext();
            }

            @Override
            public Z next() {
                return zof.apply(nextT(it1), nextT(it2));
            }

            private <T> T nextT(Iterator<T> it) {
                return it.hasNext() ? it.next() : null;
            }
        });
    }

    /**
     * Creates a limited zipped stream of two input streams. The resulting limited zipped stream contains objects created by the BiFunction<T1, T2, Z> supplied in which the first object is from the first stream, and the second object is from the second stream at the same position. The objects are filled up with null if the two streams are not of equal length. A defined maximum number of zipped pairs are generated.
     * @param s1 the first input stream.
     * @param s2 the second input stream.
     * @param limit the maximum number of pairs of objects zipped. This is useful, e.g., when one of the provided stream is infinite, and the second finite.
     * @param zof the function that zips one object from s1 and another from s2 to a zipped object.
     * @return the zipped stream.
     */
    public static <T1, T2, S1 extends BaseStream<T1, S1>, S2 extends BaseStream<T2, S2>, Z> Stream<Z> zip(S1 s1, S2 s2, long limit, BiFunction<T1, T2, Z> zof) {
        return stream(() ->  new Iterator<>() {
            long current = 0L;
            final Iterator<T1> it1 = s1.iterator();
            final Iterator<T2> it2 = s2.iterator();

            @Override
            public boolean hasNext() {
                return current < limit && (it1.hasNext() || it2.hasNext());
            }

            @Override
            public Z next() {
                current++;
                return zof.apply(nextT(it1), nextT(it2));
            }

            private <T> T nextT(Iterator<T> it) {
                if (current > limit)
                    throw new NoSuchElementException("iterator's limit exhausted!");
                return it.hasNext() ? it.next() : null;
            }
        });
    }

    /**
     * Reverses a given character sequence.
     * @param s the character sequence.
     * @return a new character sequence that represents the input characters in reverse order.
     */
    public static CharSequence reverseCharSequence(CharSequence s) {
        int len = s.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = len - 1; i >= 0; i--)
            sb.append(s.charAt(i));

        return sb;
    }

    /**
     * Converts a list of bytes to a byte array.
     * @param data the list of bytes.
     * @return the byte array.
     */
    public static byte[] transformByteListToPrimitive(List<Byte> data) {
        byte[] result = new byte[data.size()];
        for (int i = 0; i < result.length; i++)
            result[i] = data.get(i);

        return result;
    }

    /**
     * Returns a thread pool.
     * @param parallel true to return a work stealing thread pool, and false to return a single-threaded thread pool.
     * @return the thread pool.
     */
    public static ExecutorService pool(boolean parallel) {
        return parallel ? Executors.newWorkStealingPool() : Executors.newSingleThreadExecutor();
    }

    /**
     * Calculates the elapsed time to run the Runnable.
     * @param r the runnable task.
     * @return the time in milliseconds.
     */
    public static long time(Runnable r) {
        return time(Executors.callable(r)).getT1();
    }

    /**
     * Calculates the elapsed time to run the Callable. Returns a pair containing the elapsed time in milliseconds for calling the Callable and the result returned from the Callable.
     * @param callable the Callable task.
     * @return the pair of the elapsed time in milliseconds and the returned result of the Callable.
     */
    public static <T> Pair<Long, T> time(Callable<T> callable) {
        long t1 = System.currentTimeMillis();
        var t = FuncUtils.safeCall(callable);
        long time = System.currentTimeMillis() - t1;
        return new Pair<>(time, t);
    }

    /**
     * Returns a pair with the elapsed time as a pretty string describing the elapsed time to run the given Callable, and the result from that Callable. The string divides the time in days, hours, minutes, seconds, and milliseconds.
     * @param callable the Callable task.
     * @return the Pair with the pretty String describing the elapsed time, and the result returned by the Callable.
     */
    public static <T> Pair<String, T> timeAsPrettyString(Callable<T> callable) {
        var pair = time(callable);
        Duration duration = Duration.ofMillis(pair.getT1());
        return new Pair<>(asPrettyString(duration), pair.getT2());
    }

    /**
     * Returns a pretty string for the given duration. The string divides the time in days, hours, minutes, seconds, and milliseconds.
     * @param duration the Duration.
     * @return the pretty string for the duration.
     */
    public static String asPrettyString(Duration duration) {
        long millis = duration.toMillis();
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis = Math.max(0, millis - TimeUnit.DAYS.toMillis(days));

        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis = Math.max(0, millis - TimeUnit.HOURS.toMillis(hours));

        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis = Math.max(0, millis - TimeUnit.MINUTES.toMillis(minutes));

        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        millis = Math.max(0, millis - TimeUnit.SECONDS.toMillis(seconds));

        return days + " days, " + hours + " hours, " + minutes + " minutes, " + seconds + " seconds, " + millis + " millis";
    }

    /**
     * Returns a pretty string describing the elapsed time to run the given Runnable. The string divides the time in days, hours, minutes, seconds, and milliseconds.
     * @param r the Runnable task.
     * @return the pretty String describing the elapsed time.
     */
    public static String timeAsPrettyString(Runnable r) {
        return asPrettyString(Duration.ofMillis(time(r)));
    }


    /**
     * @param array the array containing all possible outcomes.
     * @return a random element in array.
     */
    public static <T> T random(T... array) {
        return array[(int) (Math.random() * array.length)];
    }

    /**
     * Deletes the file given by its path as string.
     * @param path the path as string to the file to be deleted.
     */
    public static void deleteFile(String path) {
        superSafeRun(() -> Files.delete(Paths.get(path)));
    }


    /**
     * Returns a consistent hash value for an arbitrary object. The input object can be a reference, primitive, or an array of the previous options. This method always returns the same hash value for two equal objects.
     * @param o the object for which a consistent hash value is to be computed.
     * @return the consistent hash value.
     */
    public static int consistentHash(Object o) {
        if (o == null)
            return 0;

        if (o.getClass().isArray()) {
            if (o instanceof Object[] oo)
                return Arrays.hashCode(oo);
            else if (o instanceof boolean[] oo)
                return Arrays.hashCode(oo);
            else if (o instanceof byte[] oo)
                return Arrays.hashCode(oo);
            else if (o instanceof char[] oo)
                return Arrays.hashCode(oo);
            else if (o instanceof double[] oo)
                return Arrays.hashCode(oo);
            else if (o instanceof float[] oo)
                return Arrays.hashCode(oo);
            else if (o instanceof int[] oo)
                return Arrays.hashCode(oo);
            else if (o instanceof long[] oo)
                return Arrays.hashCode(oo);
            else if (o instanceof short[] oo)
                return Arrays.hashCode(oo);
            else
                throw new AssertionError();
        }
        else
            return Objects.hashCode(o);
    }
    /**
     * Counts the number of lines in a given file.
     * @param fileName the file's path.
     * @param buffSize the buffer size used to read this file.
     * @return the number of lines.
     */
    public static int countLinesInFile(String fileName, int buffSize) {
        try (InputStream is = new BufferedInputStream(new FileInputStream(fileName))) {
            byte[] c = new byte[buffSize];

            int readChars = is.read(c);
            if (readChars == -1)
                return 0;

            int count = 0;
            while (readChars == buffSize) {
                for (int i = 0; i < buffSize; i++) {
                    if (c[i] == '\n') {
                        count++;
                    }
                }
                readChars = is.read(c);
            }

            while (readChars != -1) {
                for (int i = 0; i < readChars; i++) {
                    if (c[i] == '\n') {
                        count++;
                    }
                }
                readChars = is.read(c);
            }

            return count + 1;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Serializes a given Serializable object to a byte array.
     * @param object the Serializable object.
     * @return the byte array encoding the Serializable object.
     */
    public static byte[] serializeToByteArray(Serializable object) throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
        objOut.writeObject(object);
        objOut.close();
        return byteOut.toByteArray();
    }

    /**
     * Serializes a given Serializable object to a byte array safely, i.e., Exceptions are converted to RunTimeExceptions.
     * @param object the Serializable object.
     * @return the byte array encoding the Serializable object.
     */
    public static byte[] serializeToByteArraySafe(Serializable object) {
        return safeCall(() -> serializeToByteArray(object));
    }

    /**
     * Reads the given byte array and deserializes it to back the Java Serializable object.
     * @param byteArray the byte array encoding the Serializable object.
     * @return the deserialized object.
     * @param <T> the cast type for the deserialized object.
     */
    public static <T> T deserializeFromByteArray(byte[] byteArray) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteArray);
        ObjectInputStream objIn = new ObjectInputStream(byteIn);
        Object object = objIn.readObject();
        objIn.close();
        return (T) object;
    }


    /**
     * Reads the given byte array and deserializes it to back the Java Serializable object safely, i.e., Exceptions are converted to RunTimeExceptions.
     * @param byteArray the byte array encoding the Serializable object.
     * @return the deserialized object.
     * @param <T> the cast type for the deserialized object.
     */
    public static <T> T deserializeFromByteArraySafe(byte[] byteArray) {
        return safeCall(() -> deserializeFromByteArray(byteArray));
    }


    public static <T> T nullEscape(T t, Supplier<T> escape) {
        return conditionOrElse(Objects::nonNull, t, escape);
    }

    public static <T> T nullEscape(T t, T escape) {
        return conditionOrElse(Objects::nonNull, t, () -> escape);
    }

    public static <T> T conditionOrElse(Predicate<T> p, T t, Supplier<T> escape) {
        return p.test(t) ? t : escape.get();
    }

    public static JSONObject loadConfigFile(String configPath) {
       return new JSONObject(FuncUtils.safeCall(() -> Files.readAllLines(Path.of(configPath)).stream().filter(l -> !l.matches("[ \t]*#.*")).collect(Collectors.joining())));
    }
}
