package hr.lknezevic.brassbirmingham.network.rmi;

import hr.lknezevic.brassbirmingham.network.jndi.JndiConfig;

import javax.naming.NamingException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public final class GameServer {

    public static final int DEFAULT_PORT = 1099;

    private final String host;
    private final int port;
    private final GameRoomRegistry roomRegistry;

    private Registry rmiRegistry;
    private GameServiceImpl serviceImpl;
    private boolean running;

    public GameServer(String host, int port) {
        this.host = host;
        this.port = port;
        this.roomRegistry = new GameRoomRegistry();
    }

    public void start() throws RemoteException, NamingException {
        System.setProperty("java.rmi.server.hostname", host);
        rmiRegistry = LocateRegistry.createRegistry(port);
        serviceImpl = new GameServiceImpl(roomRegistry);
        JndiConfig.bind(serviceImpl, host, port);
        running = true;
    }

    public void stop() {
        if (!running) return;
        try {
            JndiConfig.unbind(host, port);
        } catch (NamingException ignored) {}
        try {
            java.rmi.server.UnicastRemoteObject.unexportObject(rmiRegistry, true);
        } catch (Exception ignored) {}
        running = false;
    }

    public boolean isRunning() { return running; }
    public String getHost() { return host; }
    public int getPort() { return port; }
    public GameServiceImpl getServiceImpl() { return serviceImpl; }
    public GameRoomRegistry getRoomRegistry() { return roomRegistry; }
}
