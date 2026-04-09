package hr.lknezevic.brassbirmingham.controllers;

import hr.lknezevic.brassbirmingham.app.GameSession;
import hr.lknezevic.brassbirmingham.controllers.component.HeaderController;
import hr.lknezevic.brassbirmingham.enums.SceneType;
import hr.lknezevic.brassbirmingham.model.ui.PlayerStats;
import hr.lknezevic.brassbirmingham.network.dto.LeaderboardEntry;
import hr.lknezevic.brassbirmingham.scene.SceneManager;
import hr.lknezevic.brassbirmingham.ui.ScoreboardListCell;
import hr.lknezevic.brassbirmingham.viewmodel.ScoreboardViewModel;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ScoreboardController {
    private final SceneManager sceneManager;
    private final ScoreboardViewModel scoreboardViewModel;
    private final GameSession gameSession;

    @FXML private TextField searchField;
    @FXML private ListView<PlayerStats> rankingListView;
    @FXML private HeaderController headerViewController;

    @FXML
    private void initialize() {
        if (headerViewController != null) {
            headerViewController.setTitle("Scoreboard");
            headerViewController.configureBackButton(
                    "Back to lobby",
                    () -> sceneManager.switchTo(SceneType.LOBBY)
            );
        }

        rankingListView.setItems(scoreboardViewModel.getVisibleStats());
        rankingListView.setCellFactory(
                list -> new ScoreboardListCell(scoreboardViewModel.getGlobalRanking())
        );
        rankingListView.addEventFilter(MouseEvent.ANY, Event::consume);
        rankingListView.setFocusTraversable(false);

        searchField.textProperty().addListener((obs, o, n) ->
                scoreboardViewModel.setSearchQuery(n)
        );

        loadLeaderboard();
    }

    private void loadLeaderboard() {
        if (!gameSession.isOnline()) {
            scoreboardViewModel.seedRows();
            return;
        }

        gameSession.getLeaderboard().thenAccept(entries -> Platform.runLater(() -> {
            if (entries.isEmpty()) {
                scoreboardViewModel.seedRows();
            } else {
                scoreboardViewModel.setStats(aggregateEntries(entries));
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> scoreboardViewModel.seedRows());
            return null;
        });
    }

    private List<PlayerStats> aggregateEntries(List<LeaderboardEntry> entries) {
        Map<String, List<LeaderboardEntry>> byPlayer = entries.stream()
                .collect(Collectors.groupingBy(LeaderboardEntry::getPlayerName));

        return byPlayer.entrySet().stream().map(e -> {
            String name = e.getKey();
            List<LeaderboardEntry> games = e.getValue();
            int played = games.size();
            int totalVP = games.stream().mapToInt(LeaderboardEntry::getVictoryPoints).sum();
            double avgScore = played > 0 ? (double) totalVP / played : 0;
            return new PlayerStats(name, played, played, 0, avgScore);
        }).toList();
    }
}
