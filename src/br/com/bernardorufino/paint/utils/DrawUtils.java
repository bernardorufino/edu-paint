package br.com.bernardorufino.paint.utils;

import br.com.bernardorufino.paint.ext.BitMatrix;
import javafx.scene.paint.Color;

import java.util.BitSet;

import static com.google.common.base.Preconditions.checkArgument;

public class DrawUtils {

    public static BitSet pattern(String pattern) {
        BitSet bitSet = new BitSet(pattern.length());
        char[] chars = pattern.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '1') {
                bitSet.set(chars.length - i - 1);
            }
        }
        return bitSet;
    }

    public static BitMatrix pattern2d(String pattern2d) {
        String[] patternRows = pattern2d.split("\n");
        int rows = patternRows.length;
        int columns = patternRows[0].length();
        BitMatrix bitMatrix = new BitMatrix(rows, columns);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                char c = patternRows[i].charAt(j);
                bitMatrix.set(i, j, (c != '0'));
            }
        }
        return bitMatrix;
    }

    public static Color fromIntTocolor(int color) {
        double a = ((0xFF000000 & color) >>> 24) / 255d;
        int r = (0x00FF0000 & color) >>> 16;
        int g = (0x0000FF00 & color) >>> 8;
        int b = (0x000000FF & color);
        return Color.rgb(r, g, b, a);
    }

    public static int fromColorToInt(Color color) {
        int a = (int) (Math.round(color.getOpacity() * 255));
        int r = (int) (Math.round(color.getRed() * 255));
        int g = (int) (Math.round(color.getGreen() * 255));
        int b = (int) (Math.round(color.getBlue() * 255));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static Color mergeColors(Color a, Color b, double amount) {
        checkArgument(0 <= amount && amount <= 1, "amount = " + amount + " should be btw 0 and 1 inclusive");

        return Color.rgb(
                (int) Math.round(amount * a.getRed() * 255 + (1 - amount) * b.getRed() * 255),
                (int) Math.round(amount * a.getGreen() * 255 + (1 - amount) * b.getGreen() * 255),
                (int) Math.round(amount * a.getBlue() * 255 + (1 - amount) * b.getBlue() * 255));
    }

    public static int mergeColors(int a, int b, double amount) {
        return fromColorToInt(mergeColors(fromIntTocolor(a), fromIntTocolor(b), amount));
    }

    public static int positionFor(double r, int resizeFactor) {
        int s = ((int) r);
        return s - (s % resizeFactor);
    }

    public static int indexFor(double r, int resizeFactor) {
        return ((int) r) / resizeFactor;
    }

    // Prevents instantiation
    private DrawUtils() {
        throw new AssertionError("Cannot instantiate object from " + this.getClass());
    }
}
