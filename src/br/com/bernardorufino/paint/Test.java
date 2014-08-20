package br.com.bernardorufino.paint;

import br.com.bernardorufino.paint.ui.WindowController;

public class Test {


    public static void main(String[] args) {
        boolean a = WindowController.PATTERN_CHOICES.get(1).equals(WindowController.PATTERN_CHOICES.get(0));
        System.out.println(a);
    }
}
