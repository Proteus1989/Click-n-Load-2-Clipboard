package com.suapp.cnl2c.cnl.httphandler;

import com.suapp.cnl2c.cnl.AESDecrypter;
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
import javax.management.InvalidAttributeValueException;

/**
 * Gets and decrypts Click'n Load service data
 *
 * @author Antonio
 */
public class DecrypterHandler implements HttpHandler
{

    private static final String POST_METHOD = "POST";
    private final ConcurrentLinkedDeque<Consumer<String>> linkListeners;
    private Consumer<Exception> onError = (e) -> Logger.getLogger(DecrypterHandler.class.getName()).log(Level.SEVERE, e, () -> e.getMessage());

    /**
     * Creates an instance of the class. Listener list can be modified
     * externally.
     *
     * @param linkListeners A listener list to be called when a link is
     * decrypted
     * @throws IllegalArgumentException Listener list cannot be null.
     */
    public DecrypterHandler(ConcurrentLinkedDeque<Consumer<String>> linkListeners)
    {
        if (linkListeners == null)
            throw new IllegalArgumentException("Listener cannot be null");
        this.linkListeners = linkListeners;
    }

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
     * Processes HTTP POST petition and gets decrypted links. Once links are
     * decrypted, this method will notify synchronously each listeners one by
     * one.
     *
     * @param httpExchange A Click'n Load POST request.
     * @throws UnsupportedEncodingException Thrown if POST body data is not well
     * encoded
     */
    private void processPetition(HttpExchange httpExchange) throws UnsupportedEncodingException, IllegalArgumentException
    {

        if (POST_METHOD.equals(httpExchange.getRequestMethod()))
        {
            Map<String, String> requestParamValue = handlePostRequest(httpExchange);
            
            // jk parameter is a processable js function which, after process it, return the key. But mostly key is not obfucated. Therefore, it can be extracted using a regular expression.
            String jk = requestParamValue.get("jk");
            if (jk == null)
                throw new IllegalArgumentException("jk parameter is missing");
            jk = jk.replaceAll(".+?'(.*?)'.+", "$1");
            
            String crypted = requestParamValue.get("crypted");
            if (crypted == null)
                throw new IllegalArgumentException("crypted parameter is missing");

            String decryped = AESDecrypter.decrypt(crypted, jk);

            linkListeners.forEach(c -> c.accept(decryped));
        }
    }

    /**
     * Gets POST request body, then decodes its key/value pairs and finally
     * returns it as a Map
     *
     * @param httpExchange A Click'n Load POST request.
     * @return A key/field map properly extrated and decoded.
     * @throws UnsupportedEncodingException body must be properly decoded, if
     * not this exception will be thrown.
     */
    private Map<String, String> handlePostRequest(HttpExchange httpExchange) throws UnsupportedEncodingException
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
    public DecrypterHandler setOnError(Consumer<Exception> listener)
    {
        Consumer<Exception> onErrorDefault = (e) -> Logger.getLogger(DecrypterHandler.class.getName()).log(Level.SEVERE, e, () -> e.getMessage());
        onError = onErrorDefault.andThen(listener);
        return this;
    }

}
