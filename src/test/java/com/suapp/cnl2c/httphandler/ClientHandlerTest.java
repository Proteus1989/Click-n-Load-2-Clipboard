/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suapp.cnl2c.httphandler;

import com.suapp.cnl2c.cnl.httphandler.ClientHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @author Antonio
 */
public class ClientHandlerTest
{
    @Test
    public void testHandle() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException
    {
        HttpExchange mockObject = mock(HttpExchange.class);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        when(mockObject.getResponseBody()).thenReturn(os);
        new ClientHandler().handle(mockObject);
        
        Field field = ClientHandler.class.getDeclaredField("CLIENT_RESPONSE");
        field.setAccessible(true);
        
        Assertions.assertEquals((String) field.get(null), new String(os.toByteArray(), StandardCharsets.UTF_8.name()));
        verify(mockObject, times(1)).getResponseBody();
        verify(mockObject, times(1)).sendResponseHeaders(200, ((String)field.get(null)).length());
    }
}
