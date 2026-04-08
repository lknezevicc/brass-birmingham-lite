package hr.lknezevic.brassbirmingham.controllers;

import hr.lknezevic.brassbirmingham.app.AppState;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StatusBarController {
    private final AppState appState;

    @FXML private Label connectionLabel;
    @FXML private Label roomLabel;

    @FXML
    private void initialize() {
        if (connectionLabel != null) connectionLabel.textProperty().bind(appState.connectionStatusProperty());
        if (roomLabel != null) roomLabel.textProperty().bind(appState.roomCodeProperty());
    }
}
