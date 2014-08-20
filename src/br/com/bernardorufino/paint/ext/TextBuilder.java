package br.com.bernardorufino.paint.ext;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class TextBuilder {

    public static TextBuilder withText(String text) {
        return new TextBuilder().setText(text);
    }

    public static TextBuilder withId(String id) {
        return new TextBuilder().setId(id);
    }

    private String mId;
    private String mText;

    public TextBuilder setText(String text) {
        mText = text;
        return this;
    }

    public TextBuilder setId(String id) {
        mId = id;
        return this;
    }

    public Text build() {
        Text text = new Text();
        if (mText != null) text.setText(mText);
        if (mId != null) text.setId(mId);
        return text;
    }

    public Text appendTo(TextFlow textFlow) {
        Text text = build();
        textFlow.getChildren().add(text);
        return text;
    }

    public TextFlow buildTextFlow() {
        TextFlow textFlow = new TextFlow();
        appendTo(textFlow);
        return textFlow;
    }
}
