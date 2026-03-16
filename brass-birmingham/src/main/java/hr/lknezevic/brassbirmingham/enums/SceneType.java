package hr.lknezevic.brassbirmingham.enums;

import hr.lknezevic.brassbirmingham.BrassBirminghamApplication;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.Optional;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum SceneType {

    SPLASH("views/splash-view.fxml", "styles/splash.css"),
    LOBBY("views/lobby-view.fxml", "styles/lobby.css"),
    SCOREBOARD("views/scoreboard-view.fxml", "styles/lobby.css"),
    GAME("views/game-view.fxml", "styles/game.css");

    private final String viewResourcePath;
    private final String viewCssResourcePath;

    public URL fxml() {
        log.debug("Loading FXML for scene {}", this);

        URL url = BrassBirminghamApplication.class.getResource(viewResourcePath);

        if (url == null) {
            log.error("FXML not found: {}", viewResourcePath);
            throw new IllegalStateException("FXML not found: " + viewResourcePath);
        }

        return url;
    }

    public Optional<String> css() {
        URL url = BrassBirminghamApplication.class.getResource(viewCssResourcePath);

        if (url == null) {
            log.warn("CSS not found for scene {}", this);
            return Optional.empty();
        }

        return Optional.of(url.toExternalForm());
    }
}
