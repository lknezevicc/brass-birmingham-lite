package hr.lknezevic.brassbirmingham.persistence.replay;

import java.util.Map;

public record ReplayMove(
        int playerIndex,
        String actionType,
        String era,
        int round,
        Map<String, String> params
) {}
