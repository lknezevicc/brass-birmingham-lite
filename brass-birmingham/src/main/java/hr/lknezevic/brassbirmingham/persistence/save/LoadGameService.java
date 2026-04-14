package hr.lknezevic.brassbirmingham.persistence.save;

import hr.lknezevic.brassbirmingham.logging.GameFlowLogger;
import hr.lknezevic.brassbirmingham.model.game.GameState;

import java.io.*;

public final class LoadGameService {

    public GameState load(File file) {
        GameFlowLogger.entering("file={}", file.getPath());
        if (!file.exists()) {
            throw new UncheckedIOException(new FileNotFoundException("Save file not found: " + file.getPath()));
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (!(obj instanceof GameState state)) {
                throw new IllegalStateException("Deserialized object is not a GameState: " + obj.getClass().getName());
            }
            return state;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load game from " + file.getPath(), e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class not found during deserialization", e);
        }
    }
}
