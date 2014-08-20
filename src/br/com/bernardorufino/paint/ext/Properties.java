package br.com.bernardorufino.paint.ext;

import javafx.beans.property.Property;

import java.util.function.Function;

public class Properties {

    public static <T, U> void bind(Property<T> listener, Function<U, T> transform, Property<U> subject) {
        subject.addListener((observable, oldValue, newValue) -> {
            T apply = transform.apply(newValue);
            listener.setValue(apply);
        });
    }

    // Prevents instantiation
    private Properties() {
        throw new AssertionError("Cannot instantiate object from " + this.getClass());
    }
}
