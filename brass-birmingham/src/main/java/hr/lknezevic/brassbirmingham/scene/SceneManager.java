package hr.lknezevic.brassbirmingham.scene;

import hr.lknezevic.brassbirmingham.app.AppContext;
import hr.lknezevic.brassbirmingham.enums.SceneType;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SceneManager {
    private static final double DEFAULT_WINDOW_WIDTH = 1920;
    private static final double DEFAULT_WINDOW_HEIGHT = 1080;
    private static final double MIN_WINDOW_WIDTH = 1280;
    private static final double MIN_WINDOW_HEIGHT = 720;

    private final AppContext appContext;
    private Stage stage;
    private Scene appScene;

    public SceneManager(AppContext appContext, Stage stage) {
        this.appContext = appContext;
        init(stage);
    }

    public void switchTo(SceneType sceneType) {
        ensureInitialized();
        Parent root = appContext.sceneLoader().loadRoot(sceneType);

        if (appScene == null) {
            appScene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(appScene);
            applySceneCss(sceneType, appScene);
            stage.show();
        } else {
            appScene.setRoot(root);
            applySceneCss(sceneType, appScene);

            if (!stage.isShowing()) {
                stage.show();
            }
        }
    }

    private void init(Stage stage) {
        this.stage = stage;
        this.stage.setTitle("Brass Birmingham");
        this.stage.setResizable(true);
        this.stage.setFullScreen(true);
        this.stage.setMinWidth(MIN_WINDOW_WIDTH);
        this.stage.setMinHeight(MIN_WINDOW_HEIGHT);

        this.stage.setFullScreenExitHint("Press ESC to exit fullscreen.");

        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        this.stage.setWidth(Math.min(DEFAULT_WINDOW_WIDTH, visualBounds.getWidth()));
        this.stage.setHeight(Math.min(DEFAULT_WINDOW_HEIGHT, visualBounds.getHeight()));
    }

    private void ensureInitialized() {
        if (stage == null) {
            log.error("Scene manager has not been initialized");
            throw new IllegalStateException("SceneManager is not initialized!");
        }
    }

    private void applySceneCss(SceneType sceneType, Scene appScene) {
        appScene.getStylesheets().clear();
        sceneType.css().ifPresent(appScene.getStylesheets()::add);
    }
}
