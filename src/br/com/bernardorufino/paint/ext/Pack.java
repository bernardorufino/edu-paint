package br.com.bernardorufino.paint.ext;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkState;

public class Pack {

    public static Pack readOnly(byte... bytes) {
        Pack pack = new Pack();
        pack.mData = Bytes.asList(bytes);
        return pack;
    }

    public static Pack writable(byte... bytes) {
        Pack pack = new Pack();
        pack.mData = new ArrayList<>(Bytes.asList(bytes));
        return pack;
    }

    public static final String CREATOR_NAME = "CREATOR";

    private List<Byte> mData = new ArrayList<>();
    private int mI = 0;

    private Pack() {
        /* Empty */
    }

    public byte[] readBytes(int size) {
        return Bytes.toArray(mData.subList(mI, mI += size));
    }

    public Pack writeBytes(byte... bytes) {
        List<Byte> list = Bytes.asList(bytes);
        mData.addAll(list);
        mI += list.size();
        return this;
    }

    public String readString() {
        int length = readInt();
        byte[] bytes = readBytes(length);
        return new String(bytes);
    }

    public Pack writeString(String string) {
        byte[] bytes = string.getBytes();
        writeInt(bytes.length);
        writeBytes(bytes);
        return this;
    }

    public int readInt() {
        byte[] bytes = Bytes.toArray(mData.subList(mI, mI += 4));
        return new BigInteger(bytes).intValue();
    }

    public Pack writeInt(int integer) {
        writeBytes(Ints.toByteArray(integer));
        return this;
    }

    public double readDouble() {
        return Double.parseDouble(readString());
    }

    public Pack writeDouble(double value) {
        writeString(Double.toString(value));
        return this;
    }

    public boolean readBoolean() {
        return readBytes(1)[0] == (byte) 1;
    }

    public Pack writeBoolean(boolean value) {
        writeBytes((byte) ((value) ? 1 : 0));
        return this;
    }

    public ImmutableList<Integer> readInts() {
        return readObjectList(this::readInt);
    }

    public Pack writeInts(List<Integer> integers) {
        writeInt(integers.size());
        integers.forEach(this::writeInt);
        return this;
    }

    public <T extends Persistable> T readPersistable(Persistable.Creator<T> creator) {
        readString(); // Throws id away
        return creator.create(this);
    }

    /* TODO: Create equivalent for list -> readPersistables() */
    public <T extends Persistable> T readPersistable() {
        String className = readString();
        try {
            Class<?> klass = Class.forName(className);
            Field field = klass.getField(CREATOR_NAME);
            checkState(field.getType().equals(Persistable.Creator.class), CREATOR_NAME + " field is not of type " + Persistable.Creator.class.getSimpleName());
            @SuppressWarnings("unchecked")
            Persistable.Creator<T> creator = (Persistable.Creator<T>) field.get(null);
            return creator.create(this);
        } catch (ClassNotFoundException e) {
            IllegalStateException error = new IllegalStateException("Persistable class does not exist");
            error.initCause(e);
            throw error;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            IllegalStateException error = new IllegalStateException("Persistable class does not have a " + CREATOR_NAME + " static field");
            error.initCause(e);
            throw error;
        }
    }

    public <T extends Persistable> Pack writePersistable(T persistable) {
        String className = persistable.getClass().getCanonicalName();
        writeString(className);
        persistable.persist(this);
        return this;
    }

    public <T extends Persistable> ImmutableList<T> readPersistables(Persistable.Creator<T> creator) {
        return readObjectList(() -> readPersistable(creator));
    }

    public <T extends Persistable> ImmutableList<T> readPersistables() {
        return readObjectList(this::readPersistable);
    }

    public Pack writePersistables(List<? extends Persistable> persistables) {
        writeInt(persistables.size());
        persistables.forEach(this::writePersistable);
        return this;
    }

    private <T> ImmutableList<T> readObjectList(Supplier<? extends T> supplier) {
        int size = readInt();
        ImmutableList.Builder<T> builder = ImmutableList.builder();
        for (int i = 0; i < size; i++) {
            builder.add(supplier.get());
        }
        return builder.build();
    }

    public byte[] toByteArray() {
        return Bytes.toArray(mData);
    }
}
