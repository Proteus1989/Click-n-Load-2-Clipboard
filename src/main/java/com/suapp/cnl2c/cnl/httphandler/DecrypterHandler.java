package com.suapp.cnl2c.cnl.httphandler;

import com.suapp.cnl2c.cnl.AESDecrypter;
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
public class DecrypterHandler extends AbstractHandler
{
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

}
