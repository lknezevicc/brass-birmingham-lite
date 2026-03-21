package hr.lknezevic.brassbirmingham.model.player;

import hr.lknezevic.brassbirmingham.model.card.Card;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class Hand implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int MAX_SIZE = 5;

    private final List<Card> cards = new ArrayList<>();

    public List<Card> getCards() {
        return List.copyOf(cards);
    }

    public int size() {
        return cards.size();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public void add(Card card) {
        cards.add(card);
    }

    public boolean contains(Card card) {
        return cards.contains(card);
    }

    public void remove(Card card) {
        cards.remove(card);
    }
}
