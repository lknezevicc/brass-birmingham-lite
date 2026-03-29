package hr.lknezevic.brassbirmingham.persistence;

import hr.lknezevic.brassbirmingham.engine.GameStateFactory;
import hr.lknezevic.brassbirmingham.model.action.BuildAction;
import hr.lknezevic.brassbirmingham.model.action.LoanAction;
import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.model.game.CityId;
import hr.lknezevic.brassbirmingham.model.game.GameState;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;
import hr.lknezevic.brassbirmingham.persistence.replay.ReplayDocument;
import hr.lknezevic.brassbirmingham.persistence.replay.ReplayReader;
import hr.lknezevic.brassbirmingham.persistence.replay.ReplayWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReplayReaderTest {

    @Test
    void parsesWriterOutput(@TempDir Path tempDir) {
        GameState state = GameStateFactory.newGame("Alice", "Bob");
        ReplayWriter writer = new ReplayWriter();
        writer.startGame(List.of("Alice", "Bob"));

        Card card1 = Card.location(CityId.BIRMINGHAM);
        writer.appendMove(state, 0, new LoanAction(card1));

        Card card2 = Card.location(CityId.BIRMINGHAM);
        writer.appendMove(state, 0, new BuildAction(card2, CityId.BIRMINGHAM, IndustryType.COTTON_MILL));

        writer.finishGame(1, new int[]{20, 35});

        File file = tempDir.resolve("test.xml").toFile();
        writer.writeToFile(file);

        ReplayReader reader = new ReplayReader();
        ReplayDocument doc = reader.readFromFile(file);

        assertThat(doc.getPlayerNames()).containsExactly("Alice", "Bob");
        assertThat(doc.getMapId()).isEqualTo("lite-5-cities");
        assertThat(doc.getMoves()).hasSize(2);
        assertThat(doc.getMoves().get(0).actionType()).isEqualTo("LOAN");
        assertThat(doc.getMoves().get(1).actionType()).isEqualTo("BUILD");
        assertThat(doc.getWinnerIndex()).isEqualTo(1);
        assertThat(doc.getFinalScores()).isEqualTo("20,35");
    }

    @Test
    void parsesMultipleRoundsAndEras(@TempDir Path tempDir) {
        GameState state = GameStateFactory.newGame("P1", "P2");
        ReplayWriter writer = new ReplayWriter();
        writer.startGame(List.of("P1", "P2"));

        Card card = Card.location(CityId.WOLVERHAMPTON);
        // Round 1
        writer.appendMove(state, 0, new LoanAction(card));
        writer.appendMove(state, 1, new LoanAction(card));

        writer.finishGame(0, new int[]{50, 40});

        File file = tempDir.resolve("multi.xml").toFile();
        writer.writeToFile(file);

        ReplayReader reader = new ReplayReader();
        ReplayDocument doc = reader.readFromFile(file);

        assertThat(doc.getMoves()).hasSize(2);
        assertThat(doc.getMoves().get(0).era()).isEqualTo("canal");
        assertThat(doc.getMoves().get(0).round()).isEqualTo(1);
    }

    @Test
    void rejectsXmlThatViolatesSchema(@TempDir Path tempDir) throws Exception {
        // <move> is missing the required "player" attribute, so the XSD must reject it.
        String invalid = """
                <?xml version="1.0" encoding="UTF-8"?>
                <brass-replay version="1.0">
                    <metadata>
                        <date>2026-01-01T00:00:00Z</date>
                        <players>
                            <player index="0" name="Alice"/>
                            <player index="1" name="Bob"/>
                        </players>
                        <map>lite-5-cities</map>
                        <deck-seed>0</deck-seed>
                    </metadata>
                    <era name="canal">
                        <round number="1">
                            <move action="LOAN">
                                <params><card-used>NONE</card-used></params>
                            </move>
                        </round>
                    </era>
                </brass-replay>
                """;
        File file = tempDir.resolve("invalid.xml").toFile();
        Files.writeString(file.toPath(), invalid);

        ReplayReader reader = new ReplayReader();
        assertThatThrownBy(() -> reader.readFromFile(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to parse replay XML");
    }
}
