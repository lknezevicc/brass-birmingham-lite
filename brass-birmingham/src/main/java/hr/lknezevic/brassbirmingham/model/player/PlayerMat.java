package hr.lknezevic.brassbirmingham.model.player;

import hr.lknezevic.brassbirmingham.model.industry.IndustryLevel;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;

public final class PlayerMat implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<IndustryType, Map<IndustryLevel, Integer>> tiles = new EnumMap<>(IndustryType.class);

    public PlayerMat() {
        for (IndustryType type : IndustryType.values()) {
            Map<IndustryLevel, Integer> levels = new EnumMap<>(IndustryLevel.class);
            levels.put(IndustryLevel.L1, 2);
            levels.put(IndustryLevel.L2, 2);
            tiles.put(type, levels);
        }
    }

    public int getCount(IndustryType type, IndustryLevel level) {
        return tiles.get(type).getOrDefault(level, 0);
    }

    public IndustryLevel getLowestAvailable(IndustryType type) {
        if (getCount(type, IndustryLevel.L1) > 0) return IndustryLevel.L1;
        if (getCount(type, IndustryLevel.L2) > 0) return IndustryLevel.L2;
        return null;
    }

    public boolean hasAvailable(IndustryType type) {
        return getLowestAvailable(type) != null;
    }

    public void removeTile(IndustryType type, IndustryLevel level) {
        Map<IndustryLevel, Integer> levels = tiles.get(type);
        int current = levels.getOrDefault(level, 0);
        if (current > 0) {
            levels.put(level, current - 1);
        }
    }

    public int totalRemaining() {
        return tiles.values().stream()
                .flatMapToInt(m -> m.values().stream().mapToInt(Integer::intValue))
                .sum();
    }
}
