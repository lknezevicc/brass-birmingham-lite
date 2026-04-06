package hr.lknezevic.brassbirmingham.ui;

import hr.lknezevic.brassbirmingham.model.card.Card;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

public record CardDisplayItem(Card card, String label, FontAwesomeSolid icon) {

    public static CardDisplayItem from(Card card) {
        if (card.city() != null) {
            return new CardDisplayItem(card, card.city().name(), FontAwesomeSolid.MAP_MARKER_ALT);
        }
        if (card.industry() != null) {
            FontAwesomeSolid industryIcon = switch (card.industry()) {
                case COAL_MINE -> FontAwesomeSolid.MOUNTAIN;
                case IRON_WORKS -> FontAwesomeSolid.COG;
                case BREWERY -> FontAwesomeSolid.BEER;
                case COTTON_MILL -> FontAwesomeSolid.TSHIRT;
            };
            return new CardDisplayItem(card, card.industry().name(), industryIcon);
        }
        return new CardDisplayItem(card, card.toString(), FontAwesomeSolid.QUESTION_CIRCLE);
    }
}
