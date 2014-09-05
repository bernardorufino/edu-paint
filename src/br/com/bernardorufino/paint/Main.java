package br.com.bernardorufino.paint;

import br.com.bernardorufino.paint.ext.ContextAwareController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static final String TITLE = "Paint";

    @SuppressWarnings("ConstantConditions")
    @Override
    public void start(Stage stage) throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();

        FXMLLoader fxmlLoader = new FXMLLoader(classLoader.getResource("window.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(classLoader.getResource("style.css").toString());

        ContextAwareController controller = fxmlLoader.getController();
        controller.setScene(scene).setStage(stage);

        stage.setScene(scene);
        stage.setTitle(TITLE);
        stage.setMinWidth(scene.getWidth());
        stage.setMinHeight(scene.getHeight());
        stage.setOnShown(event -> {
            stage.setMinWidth(stage.getWidth());
            stage.setMinHeight(stage.getHeight());
        });
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
