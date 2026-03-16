package hr.lknezevic.brassbirmingham.controllers.component;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.apache.commons.lang3.StringUtils;

public final class HeaderController {

    @FXML
    private Label title;

    @FXML
    private Button backButton;

    @FXML
    private void initialize() {
        // empty
    }

    public void setTitle(String titleText) {
        title.setText(StringUtils.isBlank(titleText) ? "Title" : titleText);
    }

    public void configureBackButton(String text, Runnable action) {
        backButton.setText(StringUtils.isBlank(text) ? "Button" : text);
        backButton.setOnAction(event -> action.run());
    }
}
