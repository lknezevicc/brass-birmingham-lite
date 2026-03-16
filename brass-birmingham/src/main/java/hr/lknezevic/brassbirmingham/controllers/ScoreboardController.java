package hr.lknezevic.brassbirmingham.controllers;

import hr.lknezevic.brassbirmingham.controllers.component.HeaderController;
import hr.lknezevic.brassbirmingham.enums.SceneType;
import hr.lknezevic.brassbirmingham.model.ui.PlayerStats;
import hr.lknezevic.brassbirmingham.scene.SceneManager;
import hr.lknezevic.brassbirmingham.ui.ScoreboardListCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;

@RequiredArgsConstructor
public class ScoreboardController {
    private final SceneManager sceneManager;

    private final ObservableList<PlayerStats> allStats = FXCollections.observableArrayList();
    private final SortedList<PlayerStats> globalRanking = new SortedList<>(allStats, getComparator());
    private final FilteredList<PlayerStats> filteredStats = new FilteredList<>(globalRanking, p -> true);

    @FXML
    private TextField searchField;

    @FXML
    private ListView<PlayerStats> rankingListView;

    @FXML
    private HeaderController headerViewController;

    @FXML
    private void initialize() {
        if (headerViewController != null) {
            headerViewController.setTitle("Scoreboard");
            headerViewController.configureBackButton("Back to lobby", () -> sceneManager.switchTo(SceneType.LOBBY));
        }

        seedRows();
        rankingListView.setItems(filteredStats);
        rankingListView.setCellFactory(listView -> new ScoreboardListCell(globalRanking));
        rankingListView.addEventFilter(MouseEvent.ANY, Event::consume);
        rankingListView.setFocusTraversable(false);

        searchField.textProperty().addListener((obs, oldVal, newVal) ->
                filteredStats.setPredicate(stats -> normalize(stats.username()).contains(normalize(newVal)))
        );
    }

    private void seedRows() {
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

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private Comparator<PlayerStats> getComparator() {
        return Comparator
                .comparingDouble(PlayerStats::score).reversed()
                .thenComparing(Comparator.comparingInt(PlayerStats::wins).reversed())
                .thenComparing(PlayerStats::username, String.CASE_INSENSITIVE_ORDER);
    }
}
