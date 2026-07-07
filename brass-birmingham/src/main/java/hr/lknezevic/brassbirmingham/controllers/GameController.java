package hr.lknezevic.brassbirmingham.controllers;

import hr.lknezevic.brassbirmingham.app.AppState;
import hr.lknezevic.brassbirmingham.app.GameSession;
import hr.lknezevic.brassbirmingham.controllers.game.GameActionSubmitter;
import hr.lknezevic.brassbirmingham.controllers.game.GameBoardInteractor;
import hr.lknezevic.brassbirmingham.controllers.game.GamePersistenceHandler;
import hr.lknezevic.brassbirmingham.logging.GameFlowLogger;
import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.model.game.GameState;
import hr.lknezevic.brassbirmingham.network.dto.ChatMessage;
import hr.lknezevic.brassbirmingham.network.dto.GameStateSnapshot;
import hr.lknezevic.brassbirmingham.network.dto.MoveResult;
import hr.lknezevic.brassbirmingham.scene.SceneManager;
import hr.lknezevic.brassbirmingham.ui.AnimationHelper;
import hr.lknezevic.brassbirmingham.ui.BoardActionMode;
import hr.lknezevic.brassbirmingham.ui.BoardRenderer;
import hr.lknezevic.brassbirmingham.ui.CardHandView;
import hr.lknezevic.brassbirmingham.ui.GameUiHints;
import hr.lknezevic.brassbirmingham.viewmodel.GameViewModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class GameController {
    private final AppState appState;
    private final SceneManager sceneManager;
    private final GameSession gameSession;
    private final GameViewModel viewModel;

    @FXML private Label eraLabel;
    @FXML private Label roundLabel;
    @FXML private Label turnLabel;
    @FXML private Label p1Info;
    @FXML private Label p2Info;
    @FXML private Label actionsLabel;
    @FXML private ListView<String> handList;
    @FXML private HBox cardHandContainer;
    @FXML private TextArea chatArea;
    @FXML private TextField chatInput;
    @FXML private Label statusLabel;
    @FXML private Label helpLabel;
    @FXML private Canvas boardCanvas;
    @FXML private Pane canvasPane;

    @FXML private Button loanBtn;
    @FXML private Button buildBtn;
    @FXML private Button networkBtn;
    @FXML private Button sellBtn;
    @FXML private Button scoutBtn;

    private final AnimationHelper animationHelper = new AnimationHelper();
    private final CardHandView cardHandView = new CardHandView();
    private final GamePersistenceHandler persistenceHandler = new GamePersistenceHandler();
    private GameBoardInteractor interactor;
    private GameActionSubmitter submitter;

    @FXML
    private void initialize() {
        GameFlowLogger.entering("online={}", gameSession.isOnline());
        bindLabels();

        submitter = new GameActionSubmitter(gameSession, viewModel, () -> {
            applyState(gameSession.getCurrentState());
            updateButtonStates();
        });
        interactor = new GameBoardInteractor(gameSession, viewModel, submitter, this::getSelectedOrFirstCard);

        viewModel.myTurnProperty().addListener((obs, wasMyTurn, isMyTurn) -> updateButtonStates());
        viewModel.gameOverProperty().addListener((obs, was, isOver) -> updateButtonStates());

        gameSession.setOnStateChange(this::onStateUpdated);
        gameSession.setOnChatMessage(this::onChatReceived);
        gameSession.setOnGameOver(this::onGameOver);

        initCanvas();

        if (gameSession.isOnline()) {
            gameSession.fetchState().thenAccept(snapshot -> {
                if (snapshot != null) Platform.runLater(() -> applyState(snapshot.getState()));
            });
        } else {
            applyState(gameSession.getCurrentState());
        }
    }

    private void bindLabels() {
        if (eraLabel != null) eraLabel.textProperty().bind(viewModel.eraProperty());
        if (roundLabel != null) roundLabel.textProperty().bind(viewModel.roundProperty());
        if (turnLabel != null) turnLabel.textProperty().bind(viewModel.currentTurnPlayerProperty());
        if (actionsLabel != null) actionsLabel.textProperty().bind(
                viewModel.actionsRemainingProperty().asObject().asString("Actions left: %d"));
        if (p1Info != null) p1Info.textProperty().bind(viewModel.player1InfoProperty());
        if (p2Info != null) p2Info.textProperty().bind(viewModel.player2InfoProperty());
        if (statusLabel != null) statusLabel.textProperty().bind(viewModel.statusProperty());
        if (helpLabel != null) helpLabel.setText(GameUiHints.HELP_CONTENT);
        viewModel.statusProperty().set(GameUiHints.IDLE_STATUS);
        if (cardHandContainer != null) {
            cardHandContainer.getChildren().add(cardHandView);
            viewModel.getHandItems().addListener((javafx.collections.ListChangeListener<hr.lknezevic.brassbirmingham.ui.CardDisplayItem>) c ->
                    cardHandView.update(viewModel.getHandItems()));
        }
    }

    private void initCanvas() {
        if (boardCanvas == null) return;
        BoardRenderer renderer = new BoardRenderer(boardCanvas);
        interactor.init(boardCanvas, renderer, buildBtn, networkBtn, sellBtn);
        boardCanvas.setOnMouseClicked(interactor::onCanvasClicked);
        boardCanvas.setFocusTraversable(true);
        boardCanvas.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) interactor.cancelActionMode();
        });

        if (canvasPane != null) {
            boardCanvas.widthProperty().bind(canvasPane.widthProperty());
            boardCanvas.heightProperty().bind(canvasPane.heightProperty());
            canvasPane.widthProperty().addListener((obs, o, n) -> interactor.repaintBoard());
            canvasPane.heightProperty().addListener((obs, o, n) -> interactor.repaintBoard());
        }
    }

    @FXML
    private void onBuildAction() { interactor.toggleMode(BoardActionMode.BUILD); }

    @FXML
    private void onNetworkAction() { interactor.toggleMode(BoardActionMode.NETWORK); }

    @FXML
    private void onSellAction() { interactor.toggleMode(BoardActionMode.SELL); }

    @FXML
    private void onLoanAction() {
        interactor.resetForSubmission();
        submitter.submitLoan(getSelectedOrFirstCard());
    }

    @FXML
    private void onScoutAction() {
        interactor.resetForSubmission();
        submitter.submitScout(appState);
    }

    @FXML
    private void onSendChat() { persistenceHandler.sendChat(gameSession, chatInput, chatArea); }

    @FXML
    private void onSaveGame() { persistenceHandler.saveGame(gameSession, viewModel); }

    @FXML
    private void onLoadGame() { persistenceHandler.loadGame(gameSession, viewModel, animationHelper, this::applyState); }

    private void onStateUpdated(GameStateSnapshot snapshot) { applyState(snapshot.getState()); }

    private void onChatReceived(ChatMessage msg) { persistenceHandler.onChatReceived(msg, chatArea); }

    private void onGameOver(MoveResult result) {
        GameFlowLogger.event("Game over, winner=Player {}", result.getWinnerPlayerId() + 1);
        viewModel.statusProperty().set("Game Over! Winner: Player " + (result.getWinnerPlayerId() + 1));
        viewModel.gameOverProperty().set(true);
        updateButtonStates();
        interactor.resetForSubmission();
        interactor.repaintBoard();
    }

    private void applyState(GameState state) {
        if (state == null) return;
        GameFlowLogger.event("Applying state: era={}, round={}, player={}", state.getCurrentEra(), state.getCurrentRound(), state.getCurrentPlayerIndex());
        if (!gameSession.isOnline()) {
            appState.setLocalPlayerIndex(state.getCurrentPlayerIndex());
        }
        int localIdx = appState.getLocalPlayerIndex();
        viewModel.update(state, localIdx);
        animationHelper.onStateChanged(boardCanvas, state);
        updateButtonStates();
        interactor.repaintBoard();
    }

    private void updateButtonStates() {
        boolean myTurn = viewModel.myTurnProperty().get();
        boolean over = viewModel.gameOverProperty().get();
        boolean disabled = !myTurn || over;
        for (Button b : new Button[]{loanBtn, buildBtn, networkBtn, sellBtn, scoutBtn}) {
            if (b != null) b.setDisable(disabled);
        }
        if (!myTurn && !over) viewModel.statusProperty().set("Waiting for opponent...");
        else if (myTurn && "Waiting for opponent...".equals(viewModel.statusProperty().get())) viewModel.statusProperty().set(GameUiHints.IDLE_STATUS);
    }

    private Card getSelectedOrFirstCard() {
        Card fromHand = cardHandView.getSelectedCard();
        if (fromHand != null) return fromHand;

        GameState state = gameSession.getCurrentState();
        if (state == null) return null;
        List<Card> hand = state.getPlayers().get(appState.getLocalPlayerIndex()).getHand().getCards();
        if (hand.isEmpty()) {
            viewModel.statusProperty().set("No cards in hand");
            return null;
        }
        return hand.getFirst();
    }
}
