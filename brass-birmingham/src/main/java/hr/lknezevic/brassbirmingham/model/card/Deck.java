package hr.lknezevic.brassbirmingham.model.card;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class Deck implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<Card> drawPile;
    private final List<Card> discardPile;
    private long seed;
    private int shuffleCount;

    public Deck(List<Card> cards) {
        this(cards, System.nanoTime());
    }

    public Deck(List<Card> cards, long seed) {
        this.drawPile = new ArrayList<>(cards);
        this.discardPile = new ArrayList<>();
        this.seed = seed;
        this.shuffleCount = 0;
        shuffle();
    }

    public long getSeed() { return seed; }

    public void shuffle() {
        Collections.shuffle(drawPile, new Random(seed + shuffleCount));
        shuffleCount++;
    }

    public Card draw() {
        if (drawPile.isEmpty()) {
            return null;
        }
        return drawPile.removeLast();
    }

    public void discard(Card card) {
        discardPile.add(card);
    }

    public void reshuffleBetweenEras() {
        drawPile.addAll(discardPile);
        discardPile.clear();
        shuffle();
    }

    public int drawPileSize() {
        return drawPile.size();
    }

    public int discardPileSize() {
        return discardPile.size();
    }

    public boolean isEmpty() {
        return drawPile.isEmpty();
    }
}
