package hr.lknezevic.brassbirmingham.network;

import hr.lknezevic.brassbirmingham.network.dto.GameLobby;
import hr.lknezevic.brassbirmingham.network.rmi.GameRoom;
import hr.lknezevic.brassbirmingham.network.rmi.GameRoomRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameRoomRegistryTest {

    private GameRoomRegistry registry;

    @BeforeEach
    void setup() {
        registry = new GameRoomRegistry();
    }

    @Test
    void createRoomReturnsUniqueCode() {
        GameRoom room1 = registry.createRoom("Alice");
        GameRoom room2 = registry.createRoom("Bob");
        assertThat(room1.getRoomCode()).isNotEqualTo(room2.getRoomCode());
        assertThat(room1.getRoomCode()).hasSize(4);
    }

    @Test
    void getRoomFindsCreatedRoom() {
        GameRoom room = registry.createRoom("Alice");
        assertThat(registry.getRoom(room.getRoomCode())).isNotNull();
    }

    @Test
    void roomStartsWithOnePlayer() {
        GameRoom room = registry.createRoom("Alice");
        assertThat(room.getPlayerNames()).containsExactly("Alice");
        assertThat(room.isFull()).isFalse();
    }

    @Test
    void addSecondPlayerMakesRoomFull() {
        GameRoom room = registry.createRoom("Alice");
        room.addPlayer("Bob");
        assertThat(room.isFull()).isTrue();
        assertThat(room.getPlayerNames()).containsExactly("Alice", "Bob");
    }

    @Test
    void cannotAddThirdPlayer() {
        GameRoom room = registry.createRoom("Alice");
        room.addPlayer("Bob");
        assertThatThrownBy(() -> room.addPlayer("Charlie"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void startGameCreatesEngine() {
        GameRoom room = registry.createRoom("Alice");
        room.addPlayer("Bob");
        room.startGame();
        assertThat(room.getEngine()).isNotNull();
        assertThat(room.getStatus()).isEqualTo(GameLobby.Status.IN_PROGRESS);
    }
}
