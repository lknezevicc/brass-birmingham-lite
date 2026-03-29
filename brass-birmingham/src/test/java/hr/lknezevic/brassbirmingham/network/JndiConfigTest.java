package hr.lknezevic.brassbirmingham.network;

import hr.lknezevic.brassbirmingham.network.jndi.JndiConfig;
import hr.lknezevic.brassbirmingham.network.rmi.GameServer;
import hr.lknezevic.brassbirmingham.network.rmi.GameService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JndiConfigTest {

    private static GameServer server;
    private static final int TEST_PORT = 11099;

    @BeforeAll
    static void startServer() throws Exception {
        server = new GameServer("127.0.0.1", TEST_PORT);
        server.start();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) server.stop();
    }

    @Test
    void lookupReturnsServiceProxy() throws Exception {
        GameService service = JndiConfig.lookup("127.0.0.1", TEST_PORT);
        assertThat(service).isNotNull();
    }

    @Test
    void proxyCanCreateGame() throws Exception {
        GameService service = JndiConfig.lookup("127.0.0.1", TEST_PORT);
        var lobby = service.createGame("TestPlayer");
        assertThat(lobby.getRoomCode()).hasSize(4);
    }
}
