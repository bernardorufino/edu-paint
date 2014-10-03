package br.com.bernardorufino.paint.ext;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ContextAwareController implements Initializable {

    private Property<Scene> mScene = new SimpleObjectProperty<>();
    private Property<Stage> mStage = new SimpleObjectProperty<>();
    private List<EventHandler<? super WindowEvent>> mOnShownEvents = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mStage.addListener((observable, oldValue, stage) -> {
            if (stage == null) return;
            stage.setOnShown((event) -> {
                mOnShownEvents.forEach(handler -> handler.handle(event));
            });
        });
    }

    public Scene getScene() {
        return mScene.getValue();
    }

    public ContextAwareController setScene(Scene scene) {
        mScene.setValue(scene);
        onSceneSet(scene);
        return this;
    }

    protected Property<Scene> sceneProperty() {
        return mScene;
    }

    public Stage getStage() {
        return mStage.getValue();
    }

    public ContextAwareController setStage(Stage stage) {
        mStage.setValue(stage);
        return this;
    }

    protected void onSceneSet(Scene stage) {
        /* Override */
    }

    protected Property<Stage> stageProperty() {
        return mStage;
    }

    protected void addOnShown(EventHandler<? super WindowEvent> handler) {
        mOnShownEvents.add(handler);
    }
}
