package hr.lknezevic.brassbirmingham.viewmodel;

import hr.lknezevic.brassbirmingham.engine.GameStateFactory;
import hr.lknezevic.brassbirmingham.engine.RulesEngine;
import hr.lknezevic.brassbirmingham.model.action.GameAction;
import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.model.game.GameState;
import hr.lknezevic.brassbirmingham.persistence.replay.ReplayDocument;
import hr.lknezevic.brassbirmingham.persistence.replay.ReplayMove;
import hr.lknezevic.brassbirmingham.reflection.ActionRegistry;
import hr.lknezevic.brassbirmingham.ui.CardDisplayItem;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public final class ReplayViewModel {

    private final ActionRegistry actionRegistry = new ActionRegistry();

    private ReplayDocument document;
    private int currentIndex = -1;
    private RulesEngine engine;

    private final StringProperty era = new SimpleStringProperty("-");
    private final StringProperty round = new SimpleStringProperty("-");
    private final StringProperty turn = new SimpleStringProperty("-");
    private final StringProperty moveIndex = new SimpleStringProperty("Move: 0/0");
    private final StringProperty player1Info = new SimpleStringProperty("");
    private final StringProperty player2Info = new SimpleStringProperty("");
    private final StringProperty status = new SimpleStringProperty("");
    private final BooleanProperty autoPlaying = new SimpleBooleanProperty(false);
    private final ObservableList<String> moveDescriptions = FXCollections.observableArrayList();
    private final ObservableList<CardDisplayItem> player1Hand = FXCollections.observableArrayList();
    private final ObservableList<CardDisplayItem> player2Hand = FXCollections.observableArrayList();

    public void loadDocument(ReplayDocument doc) {
        this.document = doc;
        this.currentIndex = -1;

        List<String> names = doc.getPlayerNames();
        engine = new RulesEngine(GameStateFactory.newGame(
                names.size() > 0 ? names.get(0) : "Player 1",
                names.size() > 1 ? names.get(1) : "Player 2",
                doc.getDeckSeed()));

        moveDescriptions.clear();
        for (ReplayMove m : doc.getMoves()) {
            moveDescriptions.add(formatMoveDescription(m));
        }

        refreshState();
    }

    public boolean hasNext() {
        return document != null && currentIndex < document.getMoves().size() - 1;
    }

    public boolean hasPrevious() {
        return currentIndex > -1;
    }

    public void stepForward() {
        if (!hasNext()) return;
        currentIndex++;
        ReplayMove move = document.getMoves().get(currentIndex);
        GameAction action = actionRegistry.fromXml(move.actionType(), move.params());
        List<String> errors = engine.submitAction(action);
        if (!errors.isEmpty()) {
            status.set("Replay error at move " + currentIndex + ": " + String.join(", ", errors));
        }
        refreshState();
    }

    public void stepBackward() {
        if (!hasPrevious()) return;
        currentIndex--;
        rebuildToIndex(currentIndex);
        refreshState();
    }

    public int getTotalMoves() {
        return document != null ? document.getMoves().size() : 0;
    }

    public int getCurrentIndex() { return currentIndex; }

    private void rebuildToIndex(int targetIndex) {
        List<String> names = document.getPlayerNames();
        engine = new RulesEngine(GameStateFactory.newGame(
                names.size() > 0 ? names.get(0) : "Player 1",
                names.size() > 1 ? names.get(1) : "Player 2",
                document.getDeckSeed()));

        for (int i = 0; i <= targetIndex; i++) {
            ReplayMove move = document.getMoves().get(i);
            GameAction action = actionRegistry.fromXml(move.actionType(), move.params());
            engine.submitAction(action);
        }
    }

    private void refreshState() {
        GameState state = engine.getState();
        era.set("Era: " + state.getCurrentEra());
        round.set("Round: " + state.getCurrentRound() + "/" + state.getRoundsPerEra());
        turn.set("Turn: " + state.getCurrentPlayer().getName());
        moveIndex.set("Move: " + (currentIndex + 1) + "/" + getTotalMoves());

        var p1 = state.getPlayers().get(0);
        var p2 = state.getPlayers().get(1);
        player1Info.set(String.format("%s | £%d | VP:%d", p1.getName(), p1.getMoney(), p1.getVictoryPoints()));
        player2Info.set(String.format("%s | £%d | VP:%d", p2.getName(), p2.getMoney(), p2.getVictoryPoints()));

        player1Hand.clear();
        for (Card c : p1.getHand().getCards()) player1Hand.add(CardDisplayItem.from(c));
        player2Hand.clear();
        for (Card c : p2.getHand().getCards()) player2Hand.add(CardDisplayItem.from(c));

        if (!hasNext() && document != null && document.getWinnerIndex() >= 0) {
            status.set("Game over! Winner: " + document.getPlayerNames().get(document.getWinnerIndex()));
        } else {
            status.set("");
        }
    }

    private String formatMoveDescription(ReplayMove m) {
        String player = document.getPlayerNames().size() > m.playerIndex()
                ? document.getPlayerNames().get(m.playerIndex()) : "P" + m.playerIndex();
        return String.format("[%s R%d] %s: %s", m.era(), m.round(), player, m.actionType());
    }

    public StringProperty eraProperty() { return era; }
    public StringProperty roundProperty() { return round; }
    public StringProperty turnProperty() { return turn; }
    public StringProperty moveIndexProperty() { return moveIndex; }
    public StringProperty player1InfoProperty() { return player1Info; }
    public StringProperty player2InfoProperty() { return player2Info; }
    public StringProperty statusProperty() { return status; }
    public BooleanProperty autoPlayingProperty() { return autoPlaying; }
    public ObservableList<String> getMoveDescriptions() { return moveDescriptions; }
    public ObservableList<CardDisplayItem> getPlayer1Hand() { return player1Hand; }
    public ObservableList<CardDisplayItem> getPlayer2Hand() { return player2Hand; }

    public GameState getCurrentState() {
        return engine != null ? engine.getState() : null;
    }
}
