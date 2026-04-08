package hr.lknezevic.brassbirmingham.controllers;

import hr.lknezevic.brassbirmingham.app.AppState;
import hr.lknezevic.brassbirmingham.app.GameSession;
import hr.lknezevic.brassbirmingham.enums.SceneType;
import hr.lknezevic.brassbirmingham.network.dto.GameLobby;
import hr.lknezevic.brassbirmingham.scene.SceneManager;
import hr.lknezevic.brassbirmingham.viewmodel.LobbyViewModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LobbyController {
    private final AppState appState;
    private final SceneManager sceneManager;
    private final GameSession gameSession;
    private final LobbyViewModel viewModel;

    @FXML private TextField playerNameField;
    @FXML private TextField hostField;
    @FXML private TextField portField;
    @FXML private TextField roomCodeField;
    @FXML private Label statusLabel;
    @FXML private Label roomCodeLabel;
    @FXML private Button hostBtn;
    @FXML private Button joinBtn;
    @FXML private Button localBtn;

    @FXML
    private void initialize() {
        if (playerNameField != null) playerNameField.textProperty().bindBidirectional(appState.playerNameProperty());
        if (hostField != null) hostField.textProperty().bindBidirectional(appState.networkHostProperty());
        if (portField != null) portField.textProperty().bindBidirectional(appState.networkPortProperty());
        if (statusLabel != null) statusLabel.textProperty().bind(viewModel.statusMessageProperty());
        if (roomCodeLabel != null) roomCodeLabel.textProperty().bind(viewModel.roomCodeProperty());

        viewModel.buttonsDisabledProperty().addListener((obs, was, disabled) -> setButtonsDisabled(disabled));

        appState.connectionStatusProperty().addListener((obs, old, val) -> viewModel.setStatus(val));
        appState.roomCodeProperty().addListener((obs, old, val) -> viewModel.setRoomCode(val));

        gameSession.setOnGameStarted(lobby -> sceneManager.switchTo(SceneType.GAME));
    }

    @FXML
    private void onLocalGame() {
        String name = appState.getPlayerName();
        gameSession.startLocalGame(name, "Player 2");
        sceneManager.switchTo(SceneType.GAME);
    }

    @FXML
    private void onHostGame() {
        String name = appState.getPlayerName();
        viewModel.setButtonsDisabled(true);
        viewModel.setStatus("Starting server...");

        gameSession.hostOnlineGame(name).thenAccept(lobby -> Platform.runLater(() ->
            viewModel.setStatus("Waiting for player 2...")
        )).exceptionally(ex -> {
            Platform.runLater(() -> {
                viewModel.setStatus("Error: " + ex.getMessage());
                viewModel.setButtonsDisabled(false);
            });
            return null;
        });
    }

    @FXML
    private void onJoinGame() {
        String name = appState.getPlayerName();
        String host = appState.getNetworkHost();
        int port = Integer.parseInt(appState.getNetworkPort());
        String code = roomCodeField != null ? roomCodeField.getText().trim().toUpperCase() : "";

        if (code.isEmpty()) {
            viewModel.setStatus("Enter room code");
            return;
        }

        viewModel.setButtonsDisabled(true);
        viewModel.setStatus("Connecting...");

        gameSession.joinOnlineGame(host, port, code, name).thenAccept(lobby -> Platform.runLater(() -> {
            if (lobby.getStatus() == GameLobby.Status.IN_PROGRESS) {
                sceneManager.switchTo(SceneType.GAME);
            } else {
                viewModel.setStatus("Joined, waiting for host...");
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> {
                viewModel.setStatus("Error: " + ex.getMessage());
                viewModel.setButtonsDisabled(false);
            });
            return null;
        });
    }

    @FXML
    private void onScoreboard() {
        sceneManager.switchTo(SceneType.SCOREBOARD);
    }

    @FXML
    private void onWatchReplay() {
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Open Replay");
        java.io.File replaysDir = new java.io.File("replays");
        if (replaysDir.isDirectory()) {
            fc.setInitialDirectory(replaysDir);
        }
        fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Replay XML", "*.xml"));
        java.io.File file = fc.showOpenDialog(null);
        if (file != null) {
            appState.setReplayFilePath(file.getAbsolutePath());
        }
        sceneManager.switchTo(SceneType.REPLAY);
    }

    @FXML
    private void onGenerateReport() {
        try {
            java.io.File file = new java.io.File(appState.getReportFilePath());
            new hr.lknezevic.brassbirmingham.reflection.HtmlReportGenerator().generate(file);
            viewModel.setStatus("Report generated: " + file.getAbsolutePath());
        } catch (Exception e) {
            viewModel.setStatus("Report error: " + e.getMessage());
        }
    }

    private void setButtonsDisabled(boolean disabled) {
        if (hostBtn != null) hostBtn.setDisable(disabled);
        if (joinBtn != null) joinBtn.setDisable(disabled);
        if (localBtn != null) localBtn.setDisable(disabled);
    }
}
