package hr.lknezevic.brassbirmingham.factory;

import hr.lknezevic.brassbirmingham.app.AppState;
import hr.lknezevic.brassbirmingham.controllers.GameController;
import hr.lknezevic.brassbirmingham.controllers.LobbyController;
import hr.lknezevic.brassbirmingham.controllers.SplashController;
import hr.lknezevic.brassbirmingham.controllers.StatusBarController;
import hr.lknezevic.brassbirmingham.scene.SceneManager;
import javafx.util.Callback;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.function.Supplier;

@Slf4j
public final class ControllerFactory implements Callback<Class<?>, Object> {
    private final Map<Class<?>, Supplier<?>> factories;

    public ControllerFactory(AppState appState, SceneManager sceneManager) {
        factories = Map.of(
                SplashController.class, () -> new SplashController(sceneManager),
                LobbyController.class, () -> new LobbyController(appState, sceneManager),
                GameController.class, () -> new GameController(appState, sceneManager),
                StatusBarController.class, () -> new StatusBarController(appState)
        );
    }

    @Override
    public Object call(Class<?> controllerClass) {
        Supplier<?> factory = factories.get(controllerClass);
        return (factory != null) ? factory.get() : instantiateDefault(controllerClass);
    }

    private Object instantiateDefault(Class<?> controllerClass) {
        try {
            return controllerClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            log.error("Failed to instantiate {}", controllerClass.getName(), e);
            throw new IllegalArgumentException("No DI binding for controller: " + controllerClass.getName(), e);
        }
    }
}
