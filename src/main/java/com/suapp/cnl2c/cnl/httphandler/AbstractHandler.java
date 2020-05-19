package com.suapp.cnl2c.cnl.httphandler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handler abstraction class.
 *
 * @author Antonio
 */
public abstract class AbstractHandler implements HttpHandler
{

    protected static final String POST_METHOD = "POST";
    protected final ConcurrentLinkedDeque<Consumer<String>> linkListeners;
    protected Consumer<Exception> onError = (e) -> Logger.getLogger(AbstractHandler.class.getName()).log(Level.SEVERE, e, () -> e.getMessage());

    /**
     * Creates an instance of the class. Listener list can be modified
     * externally.
     *
     * @param linkListeners A listener list to be called when a link is
     * captured
     * @throws IllegalArgumentException Listener list cannot be null.
     */
    public AbstractHandler(ConcurrentLinkedDeque<Consumer<String>> linkListeners)
    {
        if (linkListeners == null)
            throw new IllegalArgumentException("Listener cannot be null");
        this.linkListeners = linkListeners;
    }

    
    /**
     * Handles http petitions
     * 
     * @param httpExchange The incomming petition.
     * @throws IOException Thrown if something fails.
     */
    @Override
    public void handle(HttpExchange httpExchange) throws IOException
    {
        try
        {
            processPetition(httpExchange);
        } catch (Exception e)
        {
            onError.accept(new IOException("Sorry. An error occurred while link was being processed", e));
        }

    }
    
    /**
     * This method is called when a http petition is recieved.
     * 
     * @param httpExchange The incomming petition.
     * @throws Exception Any kind of exception can be thrown.
     */
    protected abstract void processPetition(HttpExchange httpExchange) throws Exception;

    /**
     * Gets POST request body, then decodes its key/value pairs and finally
     * returns it as a Map
     *
     * @param httpExchange A Click'n Load POST request.
     * @return A key/field map properly extrated and decoded.
     * @throws UnsupportedEncodingException body must be properly decoded, if
     * not this exception will be thrown.
     */
    protected Map<String, String> handlePostRequest(HttpExchange httpExchange) throws UnsupportedEncodingException
    {
        Map<String, String> body = new HashMap<>();

        String line;
        try (Scanner scan = new Scanner(httpExchange.getRequestBody()))
        {
            line = scan.nextLine();
        }

        String[] pairs = line.split("&");
        for (String pair : pairs)
        {
            int pos = pair.indexOf("=");
            body.put(pair.substring(0, pos), URLDecoder.decode(pair.substring(pos + 1), StandardCharsets.UTF_8.toString()));
        }

        return body;
    }

    /**
     * Sets an action to be perform when an error happens. If this method is
     * called twice, previous listener will be discarted.
     *
     * @param listener The action to be performed.
     * @return Current instance of ClickAndLoad
     */
    public AbstractHandler setOnError(Consumer<Exception> listener)
    {
        Consumer<Exception> onErrorDefault = (e) -> Logger.getLogger(DecrypterHandler.class.getName()).log(Level.SEVERE, e, () -> e.getMessage());
        onError = onErrorDefault.andThen(listener);
        return this;
    }

}
