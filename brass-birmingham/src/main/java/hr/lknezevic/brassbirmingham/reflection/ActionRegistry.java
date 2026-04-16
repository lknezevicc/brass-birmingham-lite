package hr.lknezevic.brassbirmingham.reflection;

import hr.lknezevic.brassbirmingham.model.action.*;
import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.model.card.CardType;
import hr.lknezevic.brassbirmingham.model.game.BoardEdge;
import hr.lknezevic.brassbirmingham.model.game.CityId;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class ActionRegistry {

    private static final String ACTION_PACKAGE = "hr.lknezevic.brassbirmingham.model.action.";

    private static final Map<String, String> ACTION_CLASS_MAP = Map.of(
            "BUILD", "BuildAction",
            "NETWORK", "NetworkAction",
            "SELL", "SellAction",
            "LOAN", "LoanAction",
            "SCOUT", "ScoutAction"
    );

    public GameAction fromXml(String actionType, Map<String, String> params) {
        String className = ACTION_CLASS_MAP.get(actionType);
        if (className == null) {
            throw new IllegalArgumentException("Unknown action type: " + actionType);
        }

        try {
            Class<?> clazz = Class.forName(ACTION_PACKAGE + className);
            return buildAction(clazz, actionType, params);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Action class not found: " + className, e);
        }
    }

    public List<String> getRegisteredTypes() {
        return List.copyOf(ACTION_CLASS_MAP.keySet());
    }

    public Class<?> getActionClass(String actionType) {
        String className = ACTION_CLASS_MAP.get(actionType);
        if (className == null) return null;
        try {
            return Class.forName(ACTION_PACKAGE + className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private GameAction buildAction(Class<?> clazz, String actionType, Map<String, String> params) {
        Card card = decodeCard(params.getOrDefault("card-used", "NONE"));

        return switch (actionType) {
            case "BUILD" -> {
                CityId city = CityId.valueOf(params.get("city"));
                IndustryType industry = IndustryType.valueOf(params.get("industry"));
                yield instantiate(clazz, card, city, industry);
            }
            case "NETWORK" -> {
                CityId cityA = CityId.valueOf(params.get("city-a"));
                CityId cityB = CityId.valueOf(params.get("city-b"));
                BoardEdge edge = new BoardEdge(cityA, cityB);
                yield instantiate(clazz, card, edge);
            }
            case "SELL" -> {
                CityId city = CityId.valueOf(params.get("city"));
                IndustryType type = IndustryType.valueOf(params.get("selling-type"));
                List<CityId> beerSources = params.containsKey("beer-sources")
                        ? Arrays.stream(params.get("beer-sources").split(","))
                            .map(CityId::valueOf).toList()
                        : List.of();
                yield instantiate(clazz, card, city, type, beerSources);
            }
            case "LOAN" -> instantiate(clazz, card);
            case "SCOUT" -> {
                Card d1 = decodeCard(params.getOrDefault("discard1", "NONE"));
                Card d2 = decodeCard(params.getOrDefault("discard2", "NONE"));
                yield instantiate(clazz, card, d1, d2);
            }
            default -> throw new IllegalArgumentException("Unhandled action type: " + actionType);
        };
    }

    @SuppressWarnings("unchecked")
    private GameAction instantiate(Class<?> clazz, Object... args) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        for (Constructor<?> ctor : constructors) {
            if (ctor.getParameterCount() == args.length) {
                try {
                    ctor.setAccessible(true);
                    return (GameAction) ctor.newInstance(args);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to instantiate " + clazz.getSimpleName(), e);
                }
            }
        }
        throw new RuntimeException("No matching constructor for " + clazz.getSimpleName()
                + " with " + args.length + " args");
    }

    static Card decodeCard(String encoded) {
        if (encoded == null || encoded.equals("NONE")) {
            return new Card(CardType.LOCATION, CityId.BIRMINGHAM, null);
        }
        String[] parts = encoded.split(":", 2);
        if (parts.length != 2) {
            return new Card(CardType.LOCATION, CityId.BIRMINGHAM, null);
        }
        return switch (parts[0]) {
            case "LOCATION" -> Card.location(CityId.valueOf(parts[1]));
            case "INDUSTRY" -> Card.industry(IndustryType.valueOf(parts[1]));
            default -> new Card(CardType.LOCATION, CityId.BIRMINGHAM, null);
        };
    }
}
