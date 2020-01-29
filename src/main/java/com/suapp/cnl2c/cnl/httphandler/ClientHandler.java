package com.suapp.cnl2c.cnl.httphandler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Simple HttpHandler to reply Click'n Load service who is asking for data.
 *
 * @author Antonio
 */
public class ClientHandler implements HttpHandler
{
    private static final String CLIENT_RESPONSE = "jdownloader=true;\nvar version='34065';";

    @Override
    public void handle(HttpExchange t) throws IOException
    {
        // JDownloader response. To know more about this line, please read ExternInterfaceImpl.jdcheckjs() method. Code available in JDownloader SVN repository)
        String response = CLIENT_RESPONSE;
        t.sendResponseHeaders(200, response.length());
        try (OutputStream os = t.getResponseBody())
        {
            os.write(response.getBytes());
        }
    }

}
