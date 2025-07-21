package datastructures.cola.run;

import datastructures.KVEntry;
import datastructures.cola.core.Run;
import datastructures.cola.core.RunBuilder;
import utils.BufferedIterator;
import utils.FuncUtils;
import utils.serializers.FixedSizeSerializer;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

public class DiskRun<K extends Comparable<K>, V> extends Run.AbstractRun<K, V> {

    private final String filePath;
    private final FileChannel fc;
    private final EntrySerializer<K, V> serializer;

    public DiskRun(String filePath, int size, int level, EntrySerializer<K, V> serializer) {
        super(size, level);
        this.fc = FuncUtils.safeCall(() -> FileChannel.open(Path.of(filePath), StandardOpenOption.READ));
        this.serializer = serializer;
        this.filePath = filePath;
    }

    @Override
    public KVEntry<K, V> get(int index) {
        return serializer.deserialize(fc, (long) index * serializer.serializedSize());
    }

    @Override
    public K getKey(int index) {
        return serializer.deserializeKey(fc,(long) index * serializer.serializedSize());
    }

    @Override
    public V getValue(int index) {
        return serializer.deserializeValue(fc, (long) index * serializer.serializedSize() + serializer.keySerializedSize());
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void deallocate() {
        FuncUtils.safeRun(fc::close);
        FuncUtils.deleteFile(filePath);
        this.size = 0;
    }

    @Override
    public BufferedIterator<KVEntry<K, V>> bufferedIterator(int numBuffedElements) {
        final int serializedSize = serializer.serializedSize();
        int buffSize = numBuffedElements * serializedSize;
        ByteBuffer buff = ByteBuffer.allocate(buffSize);
        buff.position(buffSize);

        return new BufferedIterator<>() {
            final byte[] bs = new byte[serializedSize];
            int count = 0;
            final BufferedInputStream bis = FuncUtils.safeCall(() -> new BufferedInputStream(Channels.newInputStream(fc.position(0L))));

            @Override
            public KVEntry<K, V> peek() {
                ensureSufficientBuffered();
                buff.mark();
                buff.get(bs);
                buff.reset();
                return serializer.deserialize(bs);
            }

            @Override
            public void advance() {
                ensureSufficientBuffered();
                buff.position(buff.position() + serializedSize);
                count++;
            }

            @Override
            public boolean hasNext() {
                return count < size;
            }

            @Override
            public KVEntry<K, V> next() {
                ensureSufficientBuffered();
                buff.get(bs);
                count++;
                return serializer.deserialize(bs);
            }

            private void ensureSufficientBuffered() {
                if (buff.position() >= buffSize) {
                    buff.position(0);
                    byte[] read = FuncUtils.safeCall(() -> bis.readNBytes(buff.limit()));
                    buff.put(read);
                    buff.position(0);
                    buff.limit(read.length);
                }
                if (buff.limit() < buffSize && buff.position() >= buff.limit())
                    throw new NoSuchElementException();
            }
        };
    }

   /*
    @Override
    public String toString() {
        return "DiskRun{" +
                "size=" + size +
                ", filePath='" + filePath + '\'' +
                '}';
    }

     */

    @Override
    public String toString() {
        return "DiskRun{"
                + "size=" + size
                + ", filePath='" + filePath + '\''
                + ", " + FuncUtils.stream(this::bufferedIterator).map(Objects::toString).collect(Collectors.joining(","))
                + '}';
    }

    public static class EntrySerializer<K extends Comparable<K>, V> implements FixedSizeSerializer<KVEntry<K, V>> {

        private final FixedSizeSerializer<K> keySerializer;
        private final FixedSizeSerializer<V> valueSerializer;
        private final FixedSizeSerializer<KVEntry<K, V>> fused;

        public EntrySerializer(FixedSizeSerializer<K> keySerializer, FixedSizeSerializer<V> valueSerializer) {
            this.keySerializer = keySerializer;
            this.valueSerializer = valueSerializer;
            this.fused = FixedSizeSerializer.fuse(keySerializer, valueSerializer, KVEntry::new);
        }

        public int keySerializedSize() {
            return keySerializer.serializedSize();
        }
        public int valueSerializedSize() {
            return valueSerializer.serializedSize();
        }
        @Override
        public int serializedSize() {
            return fused.serializedSize();
        }
        @Override
        public byte[] serialize(KVEntry<K, V> entry) {
            return fused.serialize(entry);
        }
        @Override
        public KVEntry<K, V> deserialize(byte[] bs) {
            return fused.deserialize(bs);
        }
        public K deserializeKey(FileChannel fc, long pos) {
            return keySerializer.deserialize(fc, pos);
        }
        public V deserializeValue(FileChannel fc, long pos) {
            return valueSerializer.deserialize(fc, pos);
        }
    }

    public static class Builder<K extends Comparable<K>, V> extends RunBuilder.AbstractRunBuilder<K, V> {
        private final BufferedOutputStream writer;
        private final String filePath;
        private final EntrySerializer<K, V> serializer;

        public Builder(String filePath, EntrySerializer<K, V> serializer, int level) {
            super(level);
            this.writer = FuncUtils.safeCall(() -> new BufferedOutputStream(new FileOutputStream(filePath)));
            this.serializer = serializer;
            this.filePath = filePath;
            this.level = level;
        }

        @Override
        public void add(KVEntry<K, V> entry) {
            FuncUtils.safeRun(() -> this.writer.write(serializer.serialize(entry)));
            size++;
        }

        @Override
        public DiskRun<K, V> build() {
            FuncUtils.safeRun(this.writer::close);
            return new DiskRun<>(filePath, size, level, serializer);
        }
    }
}
