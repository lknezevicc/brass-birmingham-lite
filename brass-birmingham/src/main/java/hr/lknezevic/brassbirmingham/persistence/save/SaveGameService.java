package hr.lknezevic.brassbirmingham.persistence.save;

import hr.lknezevic.brassbirmingham.logging.GameFlowLogger;
import hr.lknezevic.brassbirmingham.model.game.GameState;

import java.io.*;

public final class SaveGameService {

    public void save(GameState state, File file) {
        GameFlowLogger.entering("file={}", file.getPath());
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new UncheckedIOException(new IOException("Failed to create directory: " + parent));
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(state);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to save game to " + file.getPath(), e);
        }
    }
}
