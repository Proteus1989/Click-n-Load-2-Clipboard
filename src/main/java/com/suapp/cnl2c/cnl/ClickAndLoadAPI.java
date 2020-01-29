package com.suapp.cnl2c.cnl;

import com.suapp.cnl2c.cnl.httphandler.DecrypterHandler;
import com.suapp.cnl2c.cnl.httphandler.ClientHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Click'n Load service. This class will manage the service and it will inform
 * connected client
 *
 * @author Antonio
 */
public class ClickAndLoadAPI
{

    private final ConcurrentLinkedDeque<Consumer<String>> linkListeners = new ConcurrentLinkedDeque<>();
    private HttpServer server;
    private boolean running = false;
    private Consumer<Exception> onError = (e) -> Logger.getLogger(ClickAndLoadAPI.class.getName()).log(Level.SEVERE, e, () -> e.getMessage());

    private final DecrypterHandler DECRYPTERHANDLER = new DecrypterHandler(linkListeners);

    private static ClickAndLoadAPI clickAndLoad = null;

    private static final int CLICK_AND_LOAD_PORT = 9666;
    private static final String CLIENT_CONTEXT_PATH = "/jdcheck.js";
    private static final String DECRYPT_CONTEXT_PATH = "/flash/addcrypted2";

    /*
    Creates the instance in a static context, when class is loaded.
     */
    static
    {
        getInstance();
    }

    /**
     * Do nothing. Needed for singleton pattern.
     */
    private ClickAndLoadAPI()
    {
    }

    /**
     * Creates an unique instance of the class. If instance is not created the
     * method will create it. If instance is already created, the method just
     * will return it.
     *
     * @return Unique instance of the class
     */
    public static ClickAndLoadAPI getInstance()
    {
        if (clickAndLoad == null)
            clickAndLoad = new ClickAndLoadAPI();

        return clickAndLoad;
    }

    /**
     * Adds an action to be perform when a link is decrypted.
     *
     * @param listener The action to be performed.
     * @return Current instance of ClickAndLoadAPI
     * @throws IllegalArgumentException If listener argument is null,
     * IllegalArgumentException will be thrown
     */
    public ClickAndLoadAPI addListener(Consumer<String> listener)
    {
        if (listener == null)
            throw new IllegalArgumentException("Listener cannot be null");
        linkListeners.add(listener);
        return this;
    }

    /**
     * Returns a list with all added listeners.
     *
     * @return A list of listeners.
     */
    public List<Consumer<String>> getListeners()
    {
        return new LinkedList<>(linkListeners);
    }

    /**
     * Removes a current lister.
     *
     * @param listener The previuosly added action to be deleted. If listener is
     * missing this method do nothing.
     * @return Current instance of ClickAndLoadAPI
     */
    public ClickAndLoadAPI removeListener(Consumer<String> listener)
    {
        linkListeners.remove(listener);
        return this;
    }

    /**
     * Stops listening service. Listening port is released.
     */
    public void stopService()
    {
        if (server != null)
            server.stop(0);
        running = false;
    }

    /**
     * Starts listening service. Listening port is binded and contexts
     * initilizated.
     *
     * @return If service could start or not. If an error happens it will be
     * notified by setOnError listener.
     */
    public boolean startService()
    {
        stopService();
        try
        {
            server = HttpServer.create(new InetSocketAddress(CLICK_AND_LOAD_PORT), 0);
            server.createContext(CLIENT_CONTEXT_PATH, new ClientHandler());
            server.createContext(DECRYPT_CONTEXT_PATH, DECRYPTERHANDLER);
            server.setExecutor(null);
            server.start();
            running = true;
            return true;
        } catch (IOException ex)
        {
            onError.accept(new BindException("CnL2C cannot initialize listening port.\nPlease, check if either Click'n Load or other services are already listening."));
        }

        return false;
    }

    /**
     * Checks if service is running or not.
     *
     * @return If service is running or not.
     */
    public boolean isRunning()
    {
        return running;
    }

    /**
     * Sets an action to be perform when an error happens. If this method is
     * called twice, previous listener will be discarted.
     *
     * @param onError An exception consumer.
     */
    public void setOnError(Consumer<Exception> onError)
    {
        Consumer<Exception> onErrorDefault = (e) -> Logger.getLogger(ClickAndLoadAPI.class.getName()).log(Level.SEVERE, e, () -> e.getMessage());
        this.onError = onErrorDefault.andThen(onError);
    }

    /**
     * Given a crypted data and its key decrypts the data and return the result.
     * Crypted data must be encoded to Base64 and jk must be an hexadecimal
     * string
     *
     * @param crypted Base64 crypted data
     * @param jk Hexadecimal decryption key
     * @return Decrypted data
     */
    public String decrypt(String crypted, String jk)
    {
        try
        {
            String output = AESDecrypter.decrypt(crypted, jk);
            linkListeners.forEach(consumer -> consumer.accept(output));
            return output;
        } catch (Exception e)
        {
            onError.accept(e);
            throw e;
        }
    }

    /**
     * Given a crypted data and its key decrypts the data and return the result.
     *
     * @param crypted Base64 crypted data
     * @param jk Base64 decryption key
     * @return Decrypted data
     */
    public String decrypt(byte[] crypted, byte[] jk)
    {
        try
        {
            String output = AESDecrypter.decrypt(crypted, jk).trim();
            linkListeners.forEach(consumer -> consumer.accept(output));
            return output;
        } catch (Exception e)
        {
            onError.accept(e);
            throw e;
        }
    }

}
