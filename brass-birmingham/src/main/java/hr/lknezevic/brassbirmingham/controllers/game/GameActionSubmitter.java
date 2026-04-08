package hr.lknezevic.brassbirmingham.controllers.game;

import hr.lknezevic.brassbirmingham.app.AppState;
import hr.lknezevic.brassbirmingham.app.GameSession;
import hr.lknezevic.brassbirmingham.logging.GameFlowLogger;
import hr.lknezevic.brassbirmingham.model.action.GameAction;
import hr.lknezevic.brassbirmingham.model.action.LoanAction;
import hr.lknezevic.brassbirmingham.model.action.ScoutAction;
import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.model.game.GameState;
import hr.lknezevic.brassbirmingham.model.player.PlayerState;
import hr.lknezevic.brassbirmingham.ui.GameUiHints;
import hr.lknezevic.brassbirmingham.viewmodel.GameViewModel;
import javafx.application.Platform;

import java.util.List;

public final class GameActionSubmitter {

    private final GameSession gameSession;
    private final GameViewModel viewModel;
    private final Runnable onSuccess;

    public GameActionSubmitter(GameSession gameSession, GameViewModel viewModel, Runnable onSuccess) {
        this.gameSession = gameSession;
        this.viewModel = viewModel;
        this.onSuccess = onSuccess;
    }

    public void submit(GameAction action) {
        GameFlowLogger.action(action.getClass().getSimpleName(), "card={}", action.discardedCard());
        viewModel.statusProperty().set("Submitting...");

        gameSession.submitAction(action).thenAccept(result -> Platform.runLater(() -> {
            if (!result.isSuccess()) {
                GameFlowLogger.event("Action rejected: {}", result.getErrors());
                viewModel.statusProperty().set("Error: " + String.join(", ", result.getErrors()));
            } else {
                GameFlowLogger.event("Action succeeded");
                viewModel.statusProperty().set(GameUiHints.IDLE_STATUS);
                onSuccess.run();
            }
        })).exceptionally(ex -> {
            GameFlowLogger.error("Action submission failed", ex);
            Platform.runLater(() -> viewModel.statusProperty().set("Error: " + ex.getMessage()));
            return null;
        });
    }

    public void submitLoan(Card card) {
        if (card == null) return;
        submit(new LoanAction(card));
    }

    public void submitScout(AppState appState) {
        GameState state = gameSession.getCurrentState();
        if (state == null) return;
        PlayerState player = state.getPlayers().get(appState.getLocalPlayerIndex());
        List<Card> hand = player.getHand().getCards();
        if (hand.size() < 3) {
            viewModel.statusProperty().set("Need at least 3 cards for Scout");
            return;
        }
        submit(new ScoutAction(hand.get(0), hand.get(1), hand.get(2)));
    }
}
