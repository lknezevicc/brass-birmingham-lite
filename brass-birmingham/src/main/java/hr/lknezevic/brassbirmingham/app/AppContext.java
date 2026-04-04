package hr.lknezevic.brassbirmingham.app;

import hr.lknezevic.brassbirmingham.factory.ControllerFactory;
import hr.lknezevic.brassbirmingham.scene.SceneLoader;
import hr.lknezevic.brassbirmingham.scene.SceneManager;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class AppContext {
    private final AppState appState;
    private final GameSession gameSession;
    private final SceneManager sceneManager;
    private final SceneLoader sceneLoader;

    public AppContext(Stage stage) {
        this.appState = new AppState();
        this.gameSession = new GameSession(appState);
        this.sceneManager = new SceneManager(this, stage);
        this.sceneLoader = new SceneLoader(new ControllerFactory(appState, sceneManager, gameSession));
    }

    public AppState appState() { return appState; }
    public GameSession gameSession() { return gameSession; }
    public SceneManager sceneManager() { return sceneManager; }
    public SceneLoader sceneLoader() { return sceneLoader; }
}
