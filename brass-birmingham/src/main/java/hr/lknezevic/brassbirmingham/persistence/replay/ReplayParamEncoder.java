package hr.lknezevic.brassbirmingham.persistence.replay;

import hr.lknezevic.brassbirmingham.model.action.*;
import hr.lknezevic.brassbirmingham.model.card.Card;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ReplayParamEncoder {

    private ReplayParamEncoder() {}

    public static String actionTypeName(GameAction action) {
        return switch (action) {
            case BuildAction ignored -> "BUILD";
            case NetworkAction ignored -> "NETWORK";
            case SellAction ignored -> "SELL";
            case LoanAction ignored -> "LOAN";
            case ScoutAction ignored -> "SCOUT";
        };
    }

    public static Map<String, String> extractParams(GameAction action) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("card-used", encodeCard(action.discardedCard()));

        switch (action) {
            case BuildAction b -> {
                params.put("city", b.targetCity().name());
                params.put("industry", b.industryType().name());
            }
            case NetworkAction n -> {
                params.put("city-a", n.edge().getCityA().name());
                params.put("city-b", n.edge().getCityB().name());
            }
            case SellAction s -> {
                params.put("city", s.sellingCity().name());
                params.put("selling-type", s.sellingType().name());
                if (!s.beerSources().isEmpty()) {
                    params.put("beer-sources", String.join(",",
                            s.beerSources().stream().map(Enum::name).toList()));
                }
            }
            case LoanAction ignored -> { }
            case ScoutAction sc -> {
                params.put("discard1", encodeCard(sc.additionalDiscard1()));
                params.put("discard2", encodeCard(sc.additionalDiscard2()));
            }
            default -> throw new IllegalStateException("Unknown action type: " + action.getClass());
        }
        return params;
    }

    public static String encodeCard(Card card) {
        if (card == null) return "NONE";
        if (card.city() != null) return "LOCATION:" + card.city().name();
        if (card.industry() != null) return "INDUSTRY:" + card.industry().name();
        return "UNKNOWN";
    }
}
