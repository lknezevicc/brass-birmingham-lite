package hr.lknezevic.brassbirmingham.persistence;

import hr.lknezevic.brassbirmingham.engine.GameStateFactory;
import hr.lknezevic.brassbirmingham.model.action.BuildAction;
import hr.lknezevic.brassbirmingham.model.action.LoanAction;
import hr.lknezevic.brassbirmingham.model.action.NetworkAction;
import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.model.game.*;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;
import hr.lknezevic.brassbirmingham.persistence.replay.ReplayWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReplayWriterTest {

    private ReplayWriter writer;
    private GameState state;

    @BeforeEach
    void setup() {
        writer = new ReplayWriter();
        state = GameStateFactory.newGame("Alice", "Bob");
        writer.startGame(List.of("Alice", "Bob"));
    }

    @Test
    void isRecordingAfterStart() {
        assertThat(writer.isRecording()).isTrue();
    }

    @Test
    void outputContainsMetadata() {
        String xml = writer.writeToString();
        assertThat(xml).contains("<brass-replay");
        assertThat(xml).contains("version=\"1.0\"");
        assertThat(xml).contains("<metadata>");
        assertThat(xml).contains("name=\"Alice\"");
        assertThat(xml).contains("name=\"Bob\"");
        assertThat(xml).contains("<map>lite-5-cities</map>");
    }

    @Test
    void appendMoveAddsElements() {
        Card card = Card.location(CityId.BIRMINGHAM);
        writer.appendMove(state, 0, new LoanAction(card));
        String xml = writer.writeToString();

        assertThat(xml).contains("<era name=\"canal\">");
        assertThat(xml).contains("<round number=\"1\">");
        assertThat(xml).contains("action=\"LOAN\"");
        assertThat(xml).contains("player=\"0\"");
    }

    @Test
    void appendBuildMoveContainsCityAndIndustry() {
        Card card = Card.location(CityId.BIRMINGHAM);
        writer.appendMove(state, 0, new BuildAction(card, CityId.BIRMINGHAM, IndustryType.COTTON_MILL));
        String xml = writer.writeToString();

        assertThat(xml).contains("action=\"BUILD\"");
        assertThat(xml).contains("<city>BIRMINGHAM</city>");
        assertThat(xml).contains("<industry>COTTON_MILL</industry>");
    }

    @Test
    void appendNetworkMoveContainsEdge() {
        Card card = Card.location(CityId.WOLVERHAMPTON);
        BoardEdge edge = new BoardEdge(CityId.WOLVERHAMPTON, CityId.DUDLEY);
        writer.appendMove(state, 0, new NetworkAction(card, edge));
        String xml = writer.writeToString();

        assertThat(xml).contains("action=\"NETWORK\"");
        assertThat(xml).contains("<city-a>");
        assertThat(xml).contains("<city-b>");
    }

    @Test
    void finishGameAddsResult() {
        writer.finishGame(0, new int[]{42, 38});
        String xml = writer.writeToString();

        assertThat(xml).contains("<result");
        assertThat(xml).contains("winner=\"0\"");
        assertThat(xml).contains("final-scores=\"42,38\"");
    }

    @Test
    void writeToFileCreatesFile(@TempDir Path tempDir) {
        Card card = Card.location(CityId.BIRMINGHAM);
        writer.appendMove(state, 0, new LoanAction(card));
        writer.finishGame(1, new int[]{20, 30});

        File outFile = tempDir.resolve("test-replay.xml").toFile();
        writer.writeToFile(outFile);

        assertThat(outFile).exists();
        assertThat(outFile.length()).isGreaterThan(100);
    }
}
