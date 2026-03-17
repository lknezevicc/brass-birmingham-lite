package hr.lknezevic.brassbirmingham.viewmodel;

import hr.lknezevic.brassbirmingham.model.ui.PlayerStats;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import java.util.Comparator;
import java.util.List;

public final class ScoreboardViewModel {
    private static final Comparator<PlayerStats> COMPARATOR =
            Comparator.comparingDouble(PlayerStats::score).reversed()
                    .thenComparingInt(PlayerStats::wins)
                    .thenComparing(PlayerStats::username, String.CASE_INSENSITIVE_ORDER);

    private final ObservableList<PlayerStats> allStats = FXCollections.observableArrayList();
    private final SortedList<PlayerStats> globalRanking = new SortedList<>(allStats, COMPARATOR);
    private final FilteredList<PlayerStats> filteredStats = new FilteredList<>(globalRanking, p -> true);

    public ObservableList<PlayerStats> getVisibleStats() {
        return filteredStats;
    }

    public SortedList<PlayerStats> getGlobalRanking() {
        return globalRanking;
    }

    public void setSearchQuery(String query) {
        String normalized = normalize(query);

        filteredStats.setPredicate(stats ->
                normalize(stats.username()).contains(normalized)
        );
    }

    public void setStats(List<PlayerStats> stats) {
        allStats.setAll(stats);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    public void seedRows() {
        allStats.clear();
        allStats.addAll(
                new PlayerStats("IronBaron", 34, 22, 12, (double) 22 / 12),
                new PlayerStats("CanalKing", 28, 18, 10, (double) 18 / 10),
                new PlayerStats("SteamTycoon", 19, 9, 10, (double) 9 / 10),
                new PlayerStats("CoalMerchant", 41, 24, 17, (double) 24 / 17),
                new PlayerStats("PortMaster", 13, 7, 6, (double) 7 / 6),
                new PlayerStats("FoundryFox", 22, 11, 11, (double) 11 / 11),
                new PlayerStats("RailRunner", 31, 20, 11, (double) 20 / 11)
        );
    }
}
