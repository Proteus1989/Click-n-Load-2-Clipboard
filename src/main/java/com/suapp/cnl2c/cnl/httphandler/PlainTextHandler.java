package com.suapp.cnl2c.cnl.httphandler;

import com.sun.net.httpserver.HttpExchange;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

/**
 * Gets and decrypts Click'n Load service data
 *
 * @author Antonio
 */
public class PlainTextHandler extends AbstractHandler
{

    /**
     * Creates an instance of the class. Listener list can be modified
     * externally.
     *
     * @param linkListeners A listener list to be called when a link is
     * captured
     * @throws IllegalArgumentException Listener list cannot be null.
     */
    public PlainTextHandler(ConcurrentLinkedDeque<Consumer<String>> linkListeners)
    {
        super(linkListeners);
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
    @Override
    protected void processPetition(HttpExchange httpExchange) throws UnsupportedEncodingException, IllegalArgumentException
    {

        if (POST_METHOD.equals(httpExchange.getRequestMethod()))
        {
            Map<String, String> requestParamValue = handlePostRequest(httpExchange);
            
            String urls = requestParamValue.get("urls");
            if (urls == null)
                throw new IllegalArgumentException("urls parameter is missing");
            
            linkListeners.forEach(c -> c.accept(urls));
        }
    }

}
