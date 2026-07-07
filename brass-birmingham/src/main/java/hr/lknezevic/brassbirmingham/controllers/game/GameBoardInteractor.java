package hr.lknezevic.brassbirmingham.controllers.game;

import hr.lknezevic.brassbirmingham.app.GameSession;
import hr.lknezevic.brassbirmingham.logging.GameFlowLogger;
import hr.lknezevic.brassbirmingham.model.action.*;
import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.model.game.*;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;
import hr.lknezevic.brassbirmingham.ui.*;
import hr.lknezevic.brassbirmingham.viewmodel.GameViewModel;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public final class GameBoardInteractor {

    private final GameSession gameSession;
    private final GameViewModel viewModel;
    private final GameActionSubmitter submitter;
    private final Supplier<Card> selectedCardSupplier;

    private BoardRenderer boardRenderer;
    private Canvas boardCanvas;
    private BoardActionMode actionMode = BoardActionMode.NONE;

    private Button buildBtn;
    private Button networkBtn;
    private Button sellBtn;

    public GameBoardInteractor(GameSession gameSession, GameViewModel viewModel,
                               GameActionSubmitter submitter, Supplier<Card> selectedCardSupplier) {
        this.gameSession = gameSession;
        this.viewModel = viewModel;
        this.submitter = submitter;
        this.selectedCardSupplier = selectedCardSupplier;
    }

    public void init(Canvas canvas, BoardRenderer renderer, Button buildBtn, Button networkBtn, Button sellBtn) {
        this.boardCanvas = canvas;
        this.boardRenderer = renderer;
        this.buildBtn = buildBtn;
        this.networkBtn = networkBtn;
        this.sellBtn = sellBtn;
    }

    public BoardActionMode getActionMode() {
        return actionMode;
    }

    public void toggleMode(BoardActionMode mode) {
        GameFlowLogger.action("TOGGLE_MODE", "mode={}", mode);
        if (actionMode == mode) {
            cancelActionMode();
        } else {
            actionMode = mode;
            String hint = switch (mode) {
                case BUILD -> "Click a highlighted slot to build";
                case NETWORK -> "Click a highlighted edge to build a link";
                case SELL -> "Click a highlighted city to sell";
                default -> "";
            };
            viewModel.statusProperty().set(hint);
            if (boardCanvas != null) boardCanvas.requestFocus();
        }
        updateModeButtonStyles();
        repaintBoard();
    }

    public void cancelActionMode() {
        actionMode = BoardActionMode.NONE;
        viewModel.statusProperty().set(GameUiHints.IDLE_STATUS);
        updateModeButtonStyles();
        repaintBoard();
    }

    public void resetForSubmission() {
        actionMode = BoardActionMode.NONE;
        updateModeButtonStyles();
    }

    public void onCanvasClicked(MouseEvent event) {
        if (actionMode == BoardActionMode.NONE) return;
        GameState state = gameSession.getCurrentState();
        if (state == null) return;
        Card card = selectedCardSupplier.get();
        if (card == null) return;

        BoardPick pick = BoardHitDetector.pick(event.getX(), event.getY());
        GameFlowLogger.event("Canvas click: x={}, y={}, pick={}, mode={}", String.format("%.0f", event.getX()), String.format("%.0f", event.getY()), pick.kind(), actionMode);

        switch (actionMode) {
            case BUILD -> handleBuildPick(pick, card, state);
            case NETWORK -> handleNetworkPick(pick, card, state);
            case SELL -> handleSellPick(pick, card, state);
            default -> {}
        }
    }

    public void repaintBoard() {
        if (boardRenderer == null) return;
        GameState state = gameSession.getCurrentState();
        if (state == null) return;

        Card card = selectedCardSupplier.get();
        Set<CityId> highlightCities = Set.of();
        Set<BoardEdge> highlightEdges = Set.of();
        Set<SlotTarget> highlightSlots = Set.of();

        if (actionMode == BoardActionMode.BUILD && card != null) {
            highlightSlots = BoardHighlightService.getValidBuildSlots(state, card);
        } else if (actionMode == BoardActionMode.NETWORK && card != null) {
            highlightEdges = BoardHighlightService.getValidNetworkEdges(state, card);
        } else if (actionMode == BoardActionMode.SELL && card != null) {
            highlightCities = BoardHighlightService.getValidSellCities(state, card);
        }

        boardRenderer.render(state, highlightCities, highlightEdges, highlightSlots);
    }

    private void handleBuildPick(BoardPick pick, Card card, GameState state) {
        if (pick.kind() != BoardPick.Kind.SLOT) {
            viewModel.statusProperty().set("Click a highlighted slot to build");
            return;
        }
        Set<SlotTarget> valid = BoardHighlightService.getValidBuildSlots(state, card);
        SlotTarget target = new SlotTarget(pick.city(), pick.slotIndex(), pick.slotType());
        if (!valid.contains(target)) {
            viewModel.statusProperty().set("Cannot build here");
            return;
        }
        resetForSubmission();
        submitter.submit(new BuildAction(card, pick.city(), pick.slotType()));
    }

    private void handleNetworkPick(BoardPick pick, Card card, GameState state) {
        if (pick.kind() != BoardPick.Kind.EDGE) {
            viewModel.statusProperty().set("Click a highlighted edge to build a link");
            return;
        }
        Set<BoardEdge> valid = BoardHighlightService.getValidNetworkEdges(state, card);
        if (!valid.contains(pick.edge())) {
            viewModel.statusProperty().set("Cannot build link here");
            return;
        }
        resetForSubmission();
        submitter.submit(new NetworkAction(card, pick.edge()));
    }

    private void handleSellPick(BoardPick pick, Card card, GameState state) {
        CityId city = pick.city();
        if (city == null) {
            viewModel.statusProperty().set("Click a highlighted city to sell");
            return;
        }
        Set<CityId> valid = BoardHighlightService.getValidSellCities(state, card);
        if (!valid.contains(city)) {
            viewModel.statusProperty().set("Cannot sell here");
            return;
        }
        resetForSubmission();
        submitter.submit(new SellAction(card, city, IndustryType.COTTON_MILL, List.of()));
    }

    private void updateModeButtonStyles() {
        setModeStyle(buildBtn, actionMode == BoardActionMode.BUILD);
        setModeStyle(networkBtn, actionMode == BoardActionMode.NETWORK);
        setModeStyle(sellBtn, actionMode == BoardActionMode.SELL);
    }

    private static final String ACTIVE_CLASS = "active";

    private void setModeStyle(Button btn, boolean active) {
        if (btn == null) return;
        if (active) {
            if (!btn.getStyleClass().contains(ACTIVE_CLASS)) btn.getStyleClass().add(ACTIVE_CLASS);
        } else {
            btn.getStyleClass().remove(ACTIVE_CLASS);
        }
    }
}
