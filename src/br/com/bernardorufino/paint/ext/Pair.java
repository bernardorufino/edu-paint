package br.com.bernardorufino.paint.ext;

public class Pair<T, K> {

    public static <T, K> Pair of(T first, K last) {
        return new Pair<>(first, last);
    }

    public T first;
    public K last;

    private Pair(T first, K last) {
        this.first = first;
        this.last = last;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + last + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair)) return false;
        Pair pair = (Pair) o;
        if (first != null ? !first.equals(pair.first) : pair.first != null) return false;
        return !(last != null ? !last.equals(pair.last) : pair.last != null);
    }

    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (last != null ? last.hashCode() : 0);
        return result;
    }
}
