package hr.lknezevic.brassbirmingham.engine;

import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.model.card.Deck;
import hr.lknezevic.brassbirmingham.model.game.*;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;
import hr.lknezevic.brassbirmingham.model.player.Hand;
import hr.lknezevic.brassbirmingham.model.player.PlayerState;

import java.util.ArrayList;
import java.util.List;

public final class GameStateFactory {

    private GameStateFactory() {}

    public static GameState newGame(String player1Name, String player2Name) {
        return newGame(player1Name, player2Name, System.nanoTime());
    }

    public static GameState newGame(String player1Name, String player2Name, long deckSeed) {
        PlayerState p1 = new PlayerState(player1Name, 0);
        PlayerState p2 = new PlayerState(player2Name, 1);

        Deck deck = new Deck(buildDeck(), deckSeed);
        Board board = new Board();
        CityNetwork network = new CityNetwork();

        GameState state = new GameState(List.of(p1, p2), board, deck, network);

        dealInitialHands(state);

        return state;
    }

    private static List<Card> buildDeck() {
        List<Card> cards = new ArrayList<>();

        for (CityId city : CityId.values()) {
            cards.add(Card.location(city));
            cards.add(Card.location(city));
        }

        for (IndustryType type : IndustryType.values()) {
            cards.add(Card.industry(type));
            cards.add(Card.industry(type));
        }

        return cards;
    }

    private static void dealInitialHands(GameState state) {
        for (PlayerState player : state.getPlayers()) {
            for (int i = 0; i < Hand.MAX_SIZE; i++) {
                Card card = state.getDeck().draw();
                if (card != null) {
                    player.getHand().add(card);
                }
            }
        }
    }
}
