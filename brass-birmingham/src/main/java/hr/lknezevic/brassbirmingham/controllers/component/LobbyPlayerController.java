package hr.lknezevic.brassbirmingham.controllers.component;

import hr.lknezevic.brassbirmingham.app.AppState;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LobbyPlayerController {

    private final AppState appState;

    @FXML
    private TextField playerNameField;

    @FXML
    private void initialize() {
        playerNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            appState.setPlayerName(newVal == null ? "" : newVal.trim());
        });
    }
}
