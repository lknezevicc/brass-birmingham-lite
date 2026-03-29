package hr.lknezevic.brassbirmingham.serialization;

import hr.lknezevic.brassbirmingham.engine.GameStateFactory;
import hr.lknezevic.brassbirmingham.engine.RulesEngine;
import hr.lknezevic.brassbirmingham.model.action.BuildAction;
import hr.lknezevic.brassbirmingham.model.action.LoanAction;
import hr.lknezevic.brassbirmingham.model.card.Card;
import hr.lknezevic.brassbirmingham.model.game.CityId;
import hr.lknezevic.brassbirmingham.model.game.GameState;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;
import hr.lknezevic.brassbirmingham.persistence.save.LoadGameService;
import hr.lknezevic.brassbirmingham.persistence.save.SaveGameService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SaveLoadGameServiceTest {

    private final SaveGameService saveService = new SaveGameService();
    private final LoadGameService loadService = new LoadGameService();

    @Test
    void saveAndLoadPreservesGameState(@TempDir Path tempDir) {
        GameState state = GameStateFactory.newGame("Alice", "Bob");
        RulesEngine engine = new RulesEngine(state);

        Card card = state.getCurrentPlayer().getHand().getCards().getFirst();
        engine.submitAction(new LoanAction(card));

        int moneyAfter = state.getPlayers().get(0).getMoney();
        int round = state.getCurrentRound();

        File file = tempDir.resolve("test.ser").toFile();
        saveService.save(state, file);
        assertThat(file).exists();

        GameState loaded = loadService.load(file);
        assertThat(loaded.getPlayers().get(0).getMoney()).isEqualTo(moneyAfter);
        assertThat(loaded.getCurrentRound()).isEqualTo(round);
        assertThat(loaded.getPlayers().get(0).getName()).isEqualTo("Alice");
        assertThat(loaded.getPlayers().get(1).getName()).isEqualTo("Bob");
    }

    @Test
    void boardStatePreservesPlacedIndustries(@TempDir Path tempDir) {
        GameState state = GameStateFactory.newGame("Alice", "Bob");
        RulesEngine engine = new RulesEngine(state);

        Card card = state.getCurrentPlayer().getHand().getCards().getFirst();
        engine.submitAction(new LoanAction(card));

        // Give player enough money and build
        state.getPlayers().get(0).earn(50);
        Card buildCard = state.getCurrentPlayer().getHand().getCards().getFirst();
        engine.submitAction(new BuildAction(buildCard, CityId.BIRMINGHAM, IndustryType.COTTON_MILL));

        int placedCount = state.getBoard().getPlacedIndustries().size();

        File file = tempDir.resolve("board.ser").toFile();
        saveService.save(state, file);
        GameState loaded = loadService.load(file);

        assertThat(loaded.getBoard().getPlacedIndustries()).hasSize(placedCount);
    }

    @Test
    void cityNetworkIsUsableAfterLoad(@TempDir Path tempDir) {
        GameState state = GameStateFactory.newGame("X", "Y");
        File file = tempDir.resolve("net.ser").toFile();
        saveService.save(state, file);

        GameState loaded = loadService.load(file);
        // CityNetwork rebuilds its graph on deserialization
        assertThat(loaded.getCityNetwork().getAllCities()).hasSize(5);
        assertThat(loaded.getCityNetwork().getAllEdges()).isNotEmpty();
    }
}
