package hr.lknezevic.brassbirmingham.scene;

import hr.lknezevic.brassbirmingham.enums.SceneType;
import hr.lknezevic.brassbirmingham.factory.ControllerFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@RequiredArgsConstructor
public final class SceneLoader {
    private final ControllerFactory controllerFactory;

    public FXMLLoader fxmlLoader(SceneType sceneType) {
        FXMLLoader loader = new FXMLLoader(sceneType.fxml());
        loader.setControllerFactory(controllerFactory);

        return loader;
    }

    public Parent loadRoot(SceneType sceneType) {
        FXMLLoader loader = fxmlLoader(sceneType);
        try {
            return loader.load();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load scene: " + sceneType, e);
        }
    }
}
