package hr.lknezevic.brassbirmingham.viewmodel;

import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.model.game.GamePhase;
import hr.lknezevic.brassbirmingham.model.game.GameState;
import hr.lknezevic.brassbirmingham.model.player.PlayerState;
import hr.lknezevic.brassbirmingham.ui.CardDisplayItem;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public final class GameViewModel {

    private final StringProperty era = new SimpleStringProperty("-");
    private final StringProperty round = new SimpleStringProperty("-");
    private final StringProperty currentTurnPlayer = new SimpleStringProperty("-");
    private final IntegerProperty actionsRemaining = new SimpleIntegerProperty(0);
    private final StringProperty player1Info = new SimpleStringProperty("");
    private final StringProperty player2Info = new SimpleStringProperty("");
    private final BooleanProperty myTurn = new SimpleBooleanProperty(false);
    private final BooleanProperty gameOver = new SimpleBooleanProperty(false);
    private final StringProperty status = new SimpleStringProperty("");
    private final ObservableList<String> handCards = FXCollections.observableArrayList();
    private final ObservableList<CardDisplayItem> handItems = FXCollections.observableArrayList();

    public void update(GameState state, int localPlayerIndex) {
        if (state == null) return;

        era.set("Era: " + state.getCurrentEra());
        round.set("Round: " + state.getCurrentRound() + "/" + state.getRoundsPerEra());
        currentTurnPlayer.set("Turn: " + state.getCurrentPlayer().getName());
        actionsRemaining.set(state.getActionsRemainingThisTurn());

        PlayerState p1 = state.getPlayers().get(0);
        PlayerState p2 = state.getPlayers().get(1);
        player1Info.set(formatPlayer(p1));
        player2Info.set(formatPlayer(p2));

        boolean isOver = state.getPhase() == GamePhase.GAME_OVER;
        gameOver.set(isOver);
        myTurn.set(!isOver && state.getCurrentPlayerIndex() == localPlayerIndex);

        PlayerState local = state.getPlayers().get(localPlayerIndex);
        List<Card> cards = local.getHand().getCards();

        handCards.clear();
        handItems.clear();
        for (Card c : cards) {
            handCards.add(formatCard(c));
            handItems.add(CardDisplayItem.from(c));
        }
    }

    private String formatPlayer(PlayerState p) {
        return String.format("%s | £%d | Income:%d | VP:%d",
                p.getName(), p.getMoney(), p.getIncomeLevel(), p.getVictoryPoints());
    }

    private String formatCard(Card c) {
        if (c.city() != null) return "Location: " + c.city();
        if (c.industry() != null) return "Industry: " + c.industry();
        return c.toString();
    }

    public StringProperty eraProperty() { return era; }
    public StringProperty roundProperty() { return round; }
    public StringProperty currentTurnPlayerProperty() { return currentTurnPlayer; }
    public IntegerProperty actionsRemainingProperty() { return actionsRemaining; }
    public StringProperty player1InfoProperty() { return player1Info; }
    public StringProperty player2InfoProperty() { return player2Info; }
    public BooleanProperty myTurnProperty() { return myTurn; }
    public BooleanProperty gameOverProperty() { return gameOver; }
    public StringProperty statusProperty() { return status; }
    public ObservableList<String> getHandCards() { return handCards; }
    public ObservableList<CardDisplayItem> getHandItems() { return handItems; }
}
