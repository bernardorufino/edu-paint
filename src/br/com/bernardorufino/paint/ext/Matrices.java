package br.com.bernardorufino.paint.ext;

public class Matrices {

    public static <T> void forEach(T[][] matrix, MatrixConsumer<? super T> consumer) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                consumer.consume(i, j, matrix[i][j]);
            }
        }
    }

    public static void forEach(int[][] matrix, MatrixConsumer<? super Integer> consumer) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                consumer.consume(i, j, matrix[i][j]);
            }
        }
    }

    public static <T> void map(T[][] matrix, MatrixMapper<T> mapper) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j] = mapper.map(i, j, matrix[i][j]);
            }
        }
    }

    public static void map(int[][] matrix, MatrixMapper<Integer> mapper) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j] = mapper.map(i, j, matrix[i][j]);
            }
        }
    }

    // Prevents instantiation
    private Matrices() {
        throw new AssertionError("Cannot instantiate object from " + this.getClass());
    }

    public static interface MatrixConsumer<T> {

        public void consume(int i, int j, T value);
    }

    public static interface MatrixMapper<T> {

        public T map(int i, int j, T value);
    }
}
