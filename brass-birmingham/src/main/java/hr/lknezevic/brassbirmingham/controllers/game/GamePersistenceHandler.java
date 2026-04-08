package hr.lknezevic.brassbirmingham.controllers.game;

import hr.lknezevic.brassbirmingham.app.GameSession;
import hr.lknezevic.brassbirmingham.model.game.GameState;
import hr.lknezevic.brassbirmingham.network.dto.ChatMessage;
import hr.lknezevic.brassbirmingham.ui.AnimationHelper;
import hr.lknezevic.brassbirmingham.viewmodel.GameViewModel;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.function.Consumer;

public final class GamePersistenceHandler {

    public void saveGame(GameSession gameSession, GameViewModel viewModel) {
        try {
            gameSession.saveLocalGame();
            viewModel.statusProperty().set("Game saved.");
        } catch (Exception e) {
            viewModel.statusProperty().set("Save failed: " + e.getMessage());
        }
    }

    public void loadGame(GameSession gameSession, GameViewModel viewModel,
                         AnimationHelper animationHelper, Consumer<GameState> applyState) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Load Game");
        File savesDir = new File("saves");
        if (savesDir.isDirectory()) {
            fc.setInitialDirectory(savesDir);
        }
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Save files", "*.ser"));
        File file = fc.showOpenDialog(null);
        if (file == null) return;
        try {
            gameSession.loadLocalGame(file);
            animationHelper.reset();
            GameState state = gameSession.getCurrentState();
            if (state != null) applyState.accept(state);
            viewModel.statusProperty().set("Game loaded.");
        } catch (Exception e) {
            viewModel.statusProperty().set("Load failed: " + e.getMessage());
        }
    }

    public void sendChat(GameSession gameSession, TextField chatInput, TextArea chatArea) {
        if (chatInput == null || chatInput.getText().isBlank()) return;
        String msg = chatInput.getText().trim();
        gameSession.sendChat(msg);
        if (chatArea != null) chatArea.appendText("You: " + msg + "\n");
        chatInput.clear();
    }

    public void onChatReceived(ChatMessage msg, TextArea chatArea) {
        if (chatArea != null) chatArea.appendText(msg.getSender() + ": " + msg.getText() + "\n");
    }
}
