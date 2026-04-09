package hr.lknezevic.brassbirmingham.factory;

import hr.lknezevic.brassbirmingham.app.AppState;
import hr.lknezevic.brassbirmingham.app.GameSession;
import hr.lknezevic.brassbirmingham.controllers.*;
import hr.lknezevic.brassbirmingham.controllers.component.LobbyPlayerController;
import hr.lknezevic.brassbirmingham.logging.GameFlowLogger;
import hr.lknezevic.brassbirmingham.scene.SceneManager;
import hr.lknezevic.brassbirmingham.viewmodel.GameViewModel;
import hr.lknezevic.brassbirmingham.viewmodel.LobbyViewModel;
import hr.lknezevic.brassbirmingham.viewmodel.ReplayViewModel;
import hr.lknezevic.brassbirmingham.viewmodel.ScoreboardViewModel;
import javafx.util.Callback;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
public final class ControllerFactory implements Callback<Class<?>, Object> {
    private final Map<Class<?>, Supplier<?>> factories;

    public ControllerFactory(AppState appState, SceneManager sceneManager, GameSession gameSession) {
        GameViewModel gameViewModel = new GameViewModel();
        LobbyViewModel lobbyViewModel = new LobbyViewModel();
        ScoreboardViewModel scoreboardViewModel = new ScoreboardViewModel();
        ReplayViewModel replayViewModel = new ReplayViewModel();

        factories = new HashMap<>();
        factories.put(SplashController.class, () -> new SplashController(sceneManager));
        factories.put(LobbyController.class, () -> new LobbyController(appState, sceneManager, gameSession, lobbyViewModel));
        factories.put(ScoreboardController.class, () -> new ScoreboardController(sceneManager, scoreboardViewModel, gameSession));
        factories.put(GameController.class, () -> new GameController(appState, sceneManager, gameSession, gameViewModel));
        factories.put(StatusBarController.class, () -> new StatusBarController(appState));
        factories.put(LobbyPlayerController.class, () -> new LobbyPlayerController(appState));
        factories.put(ReplayController.class, () -> new ReplayController(appState, sceneManager, replayViewModel));
    }

    @Override
    public Object call(Class<?> controllerClass) {
        GameFlowLogger.event("Creating controller: {}", controllerClass.getSimpleName());
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
