package hr.lknezevic.brassbirmingham.controllers;

import hr.lknezevic.brassbirmingham.controllers.component.HeaderController;
import hr.lknezevic.brassbirmingham.enums.SceneType;
import hr.lknezevic.brassbirmingham.model.ui.PlayerStats;
import hr.lknezevic.brassbirmingham.scene.SceneManager;
import hr.lknezevic.brassbirmingham.ui.ScoreboardListCell;
import hr.lknezevic.brassbirmingham.viewmodel.ScoreboardViewModel;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ScoreboardController {
    private final SceneManager sceneManager;
    private final ScoreboardViewModel scoreboardViewModel;

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
            headerViewController.configureBackButton(
                    "Back to lobby",
                    () -> sceneManager.switchTo(SceneType.LOBBY)
            );
        }

        // TODO remove; just for testing
        scoreboardViewModel.seedRows();

        rankingListView.setItems(scoreboardViewModel.getVisibleStats());
        rankingListView.setCellFactory(
                list -> new ScoreboardListCell(scoreboardViewModel.getGlobalRanking())
        );

        rankingListView.addEventFilter(MouseEvent.ANY, Event::consume);
        rankingListView.setFocusTraversable(false);

        searchField.textProperty().addListener((obs, o, n) ->
                scoreboardViewModel.setSearchQuery(n)
        );
    }
}
