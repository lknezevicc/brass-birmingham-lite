package hr.lknezevic.brassbirmingham.network.rmi;

import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;

public final class GameRoomRegistry {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 4;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ConcurrentHashMap<String, GameRoom> rooms = new ConcurrentHashMap<>();

    public GameRoom createRoom(String hostName) {
        String code = generateUniqueCode();
        GameRoom room = new GameRoom(code, hostName);
        rooms.put(code, room);
        return room;
    }

    public GameRoom getRoom(String code) {
        return rooms.get(code.toUpperCase());
    }

    public void removeRoom(String code) {
        rooms.remove(code.toUpperCase());
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = randomCode();
        } while (rooms.containsKey(code));
        return code;
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }
}
