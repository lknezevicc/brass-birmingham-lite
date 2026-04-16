package hr.lknezevic.brassbirmingham.controllers;

import hr.lknezevic.brassbirmingham.app.AppState;
import hr.lknezevic.brassbirmingham.enums.SceneType;
import hr.lknezevic.brassbirmingham.model.game.GameState;
import hr.lknezevic.brassbirmingham.persistence.replay.ReplayDocument;
import hr.lknezevic.brassbirmingham.persistence.replay.ReplayReader;
import hr.lknezevic.brassbirmingham.scene.SceneManager;
import hr.lknezevic.brassbirmingham.ui.AnimationHelper;
import hr.lknezevic.brassbirmingham.ui.BoardRenderer;
import hr.lknezevic.brassbirmingham.ui.CardHandView;
import hr.lknezevic.brassbirmingham.viewmodel.ReplayViewModel;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import lombok.RequiredArgsConstructor;

import java.io.File;

@RequiredArgsConstructor
public class ReplayController {
    private final AppState appState;
    private final SceneManager sceneManager;
    private final ReplayViewModel viewModel;

    @FXML private Label eraLabel;
    @FXML private Label roundLabel;
    @FXML private Label turnLabel;
    @FXML private Label moveIndexLabel;
    @FXML private Label p1Info;
    @FXML private Label p2Info;
    @FXML private Label statusLabel;
    @FXML private ListView<String> moveList;
    @FXML private Button prevBtn;
    @FXML private Button nextBtn;
    @FXML private Button autoPlayBtn;
    @FXML private Canvas replayCanvas;
    @FXML private Pane replayCanvasPane;
    @FXML private HBox p1HandContainer;
    @FXML private HBox p2HandContainer;

    private BoardRenderer boardRenderer;
    private final AnimationHelper animationHelper = new AnimationHelper();
    private final CardHandView p1HandView = new CardHandView(true);
    private final CardHandView p2HandView = new CardHandView(true);
    private PauseTransition autoPlayTimer;

    @FXML
    private void initialize() {
        if (eraLabel != null) eraLabel.textProperty().bind(viewModel.eraProperty());
        if (roundLabel != null) roundLabel.textProperty().bind(viewModel.roundProperty());
        if (turnLabel != null) turnLabel.textProperty().bind(viewModel.turnProperty());
        if (moveIndexLabel != null) moveIndexLabel.textProperty().bind(viewModel.moveIndexProperty());
        if (p1Info != null) p1Info.textProperty().bind(viewModel.player1InfoProperty());
        if (p2Info != null) p2Info.textProperty().bind(viewModel.player2InfoProperty());
        if (statusLabel != null) statusLabel.textProperty().bind(viewModel.statusProperty());
        if (moveList != null) moveList.setItems(viewModel.getMoveDescriptions());

        if (p1HandContainer != null) p1HandContainer.getChildren().add(p1HandView);
        if (p2HandContainer != null) p2HandContainer.getChildren().add(p2HandView);

        if (replayCanvas != null) {
            boardRenderer = new BoardRenderer(replayCanvas);
            if (replayCanvasPane != null) {
                replayCanvas.widthProperty().bind(replayCanvasPane.widthProperty());
                replayCanvas.heightProperty().bind(replayCanvasPane.heightProperty());
                replayCanvasPane.widthProperty().addListener((obs, o, n) -> repaintBoard());
                replayCanvasPane.heightProperty().addListener((obs, o, n) -> repaintBoard());
            }
        }

        loadReplay();
        updateButtons();
    }

    @FXML
    private void onNext() {
        viewModel.stepForward();
        animationHelper.onStateChanged(replayCanvas, viewModel.getCurrentState());
        updateButtons();
        scrollToCurrentMove();
        refreshHands();
        repaintBoard();
    }

    @FXML
    private void onPrevious() {
        viewModel.stepBackward();
        updateButtons();
        scrollToCurrentMove();
        refreshHands();
        repaintBoard();
    }

    @FXML
    private void onAutoPlay() {
        if (viewModel.autoPlayingProperty().get()) {
            stopAutoPlay();
        } else {
            startAutoPlay();
        }
    }

    @FXML
    private void onBackToLobby() {
        stopAutoPlay();
        sceneManager.switchTo(SceneType.LOBBY);
    }

    private void loadReplay() {
        String path = appState.getReplayFilePath();
        File file = new File(path);
        if (!file.exists()) {
            viewModel.statusProperty().set("No replay file found: " + path);
            return;
        }
        ReplayReader reader = new ReplayReader();
        ReplayDocument doc = reader.readFromFile(file);
        viewModel.loadDocument(doc);
        animationHelper.reset();
        refreshHands();
        repaintBoard();
    }

    private void repaintBoard() {
        if (boardRenderer == null) return;
        GameState state = viewModel.getCurrentState();
        if (state != null) {
            boardRenderer.render(state);
        }
    }

    private void refreshHands() {
        p1HandView.update(viewModel.getPlayer1Hand());
        p2HandView.update(viewModel.getPlayer2Hand());
    }

    private void startAutoPlay() {
        viewModel.autoPlayingProperty().set(true);
        if (autoPlayBtn != null) autoPlayBtn.setText("Stop");
        autoPlayTimer = new PauseTransition(Duration.seconds(1));
        autoPlayTimer.setOnFinished(e -> {
            if (viewModel.hasNext()) {
                viewModel.stepForward();
                animationHelper.onStateChanged(replayCanvas, viewModel.getCurrentState());
                updateButtons();
                scrollToCurrentMove();
                refreshHands();
                repaintBoard();
                autoPlayTimer.playFromStart();
            } else {
                stopAutoPlay();
            }
        });
        autoPlayTimer.play();
    }

    private void stopAutoPlay() {
        viewModel.autoPlayingProperty().set(false);
        if (autoPlayBtn != null) autoPlayBtn.setText("Auto-play");
        if (autoPlayTimer != null) {
            autoPlayTimer.stop();
            autoPlayTimer = null;
        }
    }

    private void updateButtons() {
        if (prevBtn != null) prevBtn.setDisable(!viewModel.hasPrevious());
        if (nextBtn != null) nextBtn.setDisable(!viewModel.hasNext());
    }

    private void scrollToCurrentMove() {
        if (moveList != null && viewModel.getCurrentIndex() >= 0) {
            moveList.scrollTo(viewModel.getCurrentIndex());
            moveList.getSelectionModel().select(viewModel.getCurrentIndex());
        }
    }
}
