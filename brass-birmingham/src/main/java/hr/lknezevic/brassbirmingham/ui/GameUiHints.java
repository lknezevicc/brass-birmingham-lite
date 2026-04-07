package hr.lknezevic.brassbirmingham.ui;

public final class GameUiHints {

    private GameUiHints() {}

    public static final String IDLE_STATUS =
            "Select a card, then choose an action. Press Esc to cancel.";

    public static final String HELP_CONTENT = """
            BUILD — Location card: build in that city. \
            Industry card: build that type anywhere in your network. \
            Click a highlighted slot under the city.

            NETWORK — Discard any card. Click a highlighted edge. \
            Edge must touch your network (all edges valid at game start). \
            Canal: £3 | Rail: £5 + 1 coal.

            SELL — Click a highlighted city to sell Cotton Mill. \
            Requires beer and a link to the merchant.

            LOAN — Discards selected card, receive £20, lose 2 income. \
            No board click needed.

            SCOUT — Discards 3 cards from hand, draw 1 new card. \
            No board click needed.""";

    public static final String TIP_BUILD =
            "Location card → slots in that city. Industry card → that type in your network.";
    public static final String TIP_NETWORK =
            "Discard any card. Place a link on an edge next to your network.";
    public static final String TIP_SELL =
            "Click a city with your Cotton Mill to sell. Needs beer + merchant link.";
    public static final String TIP_LOAN =
            "Discard a card. Receive £20, lose 2 income.";
    public static final String TIP_SCOUT =
            "Discard 3 cards, draw 1 new one.";
}
