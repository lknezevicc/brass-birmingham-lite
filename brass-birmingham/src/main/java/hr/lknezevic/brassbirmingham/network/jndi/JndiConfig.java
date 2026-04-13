package hr.lknezevic.brassbirmingham.network.jndi;

import hr.lknezevic.brassbirmingham.network.rmi.GameService;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;

public final class JndiConfig {

    private static final String SERVICE_NAME = "brass/GameService";

    private JndiConfig() {}

    public static void bind(GameService service, String host, int port) throws NamingException {
        Context ctx = createContext(host, port);
        ctx.rebind(SERVICE_NAME, service);
        ctx.close();
    }

    public static void unbind(String host, int port) throws NamingException {
        Context ctx = createContext(host, port);
        ctx.unbind(SERVICE_NAME);
        ctx.close();
    }

    public static GameService lookup(String host, int port) throws NamingException {
        Context ctx = createContext(host, port);
        GameService service = (GameService) ctx.lookup(SERVICE_NAME);
        ctx.close();
        return service;
    }

    private static Context createContext(String host, int port) throws NamingException {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
        env.put(Context.PROVIDER_URL, "rmi://" + host + ":" + port);
        return new InitialContext(env);
    }
}
