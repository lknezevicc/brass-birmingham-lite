package hr.lknezevic.brassbirmingham.ui;

import hr.lknezevic.brassbirmingham.model.ui.PlayerStats;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

import java.util.List;

public final class ScoreboardListCell extends ListCell<PlayerStats> {

    private static final double MIN_RANK_WIDTH = 70;
    private static final double MIN_USERNAME_WIDTH = 280;
    private static final double DEFAULT_MIN_WIDTH = 120;

    private final HBox container = new HBox(8);

    private final Label rank = initializeLabel(MIN_RANK_WIDTH);
    private final Label username = initializeLabel(MIN_USERNAME_WIDTH);
    private final Label played = initializeLabel(DEFAULT_MIN_WIDTH);
    private final Label wins = initializeLabel(DEFAULT_MIN_WIDTH);
    private final Label losses = initializeLabel(DEFAULT_MIN_WIDTH);
    private final Label score = initializeLabel(DEFAULT_MIN_WIDTH);

    private final List<PlayerStats> globalRanking;

    public ScoreboardListCell(List<PlayerStats> globalRanking) {
        this.globalRanking = globalRanking;

        container.setAlignment(Pos.CENTER_LEFT);

        rank.setStyle("-fx-font-weight: 700;");
        score.setStyle("-fx-font-weight: 700;");

        container.getChildren().addAll(rank, username, played, wins, losses, score);
        container.getChildren().forEach(node -> node.getStyleClass().add("scoreboard-text"));
    }

    @Override
    protected void updateItem(PlayerStats stats, boolean empty) {
        super.updateItem(stats, empty);

        if (empty || stats == null) {
            setGraphic(null);
            return;
        }

        int rankIndex = globalRanking.indexOf(stats) + 1;

        rank.setText("#" + rankIndex);
        username.setText(stats.username());
        played.setText(String.valueOf(stats.played()));
        wins.setText(String.valueOf(stats.wins()));
        losses.setText(String.valueOf(stats.losses()));
        score.setText(String.format("%.2f", stats.score()));

        setGraphic(container);
    }

    private Label initializeLabel(double width) {
        Label label = new Label("");
        label.setMinWidth(width);
        label.setPrefWidth(width);
        return label;
    }
}
