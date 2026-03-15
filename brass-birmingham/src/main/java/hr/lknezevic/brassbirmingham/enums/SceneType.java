package hr.lknezevic.brassbirmingham.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.Optional;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum SceneType {

    SPLASH("splash-view.fxml", "css/splash.css"),
    LOBBY("views/lobby-view.fxml", "css/lobby.css"),
    GAME("views/game-view.fxml", "css/game.css");

    private final String viewResourcePath;
    private final String viewCssResourcePath;

    public URL fxml() {
        log.debug("Loading FXML for scene {}", this);

        URL url = SceneType.class.getResource(viewResourcePath);

        if (url == null) {
            log.error("FXML not found: {}", viewResourcePath);
            throw new IllegalStateException("FXML not found: " + viewResourcePath);
        }

        return url;
    }

    public Optional<String> css() {
        URL url = SceneType.class.getResource(viewCssResourcePath);

        if (url == null) {
            log.warn("CSS not found for scene {}", this);
            return Optional.empty();
        }

        return Optional.of(url.toExternalForm());
    }
}
