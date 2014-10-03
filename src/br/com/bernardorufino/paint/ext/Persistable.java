package br.com.bernardorufino.paint.ext;

public interface Persistable {

    public abstract void persist(Pack out);

    public static abstract class Creator<T> {

        public abstract T create(Pack in);
    }

}
