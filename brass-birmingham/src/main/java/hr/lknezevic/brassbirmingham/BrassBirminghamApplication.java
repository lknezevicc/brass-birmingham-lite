package hr.lknezevic.brassbirmingham;

import hr.lknezevic.brassbirmingham.app.AppContext;
import hr.lknezevic.brassbirmingham.enums.SceneType;
import javafx.application.Application;
import javafx.stage.Stage;

public class BrassBirminghamApplication extends Application {

    @Override
    public void start(Stage stage) {
        new AppContext(stage).sceneManager().switchTo(SceneType.SPLASH);
    }

    public static void main(String[] args) {
        launch();
    }
}