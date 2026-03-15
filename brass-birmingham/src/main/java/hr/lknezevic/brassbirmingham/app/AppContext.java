package hr.lknezevic.brassbirmingham.app;

import hr.lknezevic.brassbirmingham.factory.ControllerFactory;
import hr.lknezevic.brassbirmingham.scene.SceneLoader;
import hr.lknezevic.brassbirmingham.scene.SceneManager;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppContext {
    private final SceneManager sceneManager;
    private final SceneLoader sceneLoader;

    public AppContext(Stage stage) {
        this.sceneManager = new SceneManager(this, stage);
        this.sceneLoader = new SceneLoader(new ControllerFactory(new AppState(), sceneManager));
    }

    public SceneManager sceneManager() {
        return sceneManager;
    }

    public SceneLoader sceneLoader() {
        return sceneLoader;
    }
}
