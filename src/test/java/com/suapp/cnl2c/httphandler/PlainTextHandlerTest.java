/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suapp.cnl2c.httphandler;

import com.suapp.cnl2c.cnl.httphandler.AbstractHandler;
import com.suapp.cnl2c.cnl.httphandler.PlainTextHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.AdditionalAnswers.delegatesTo;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @author Antonio
 */
public class PlainTextHandlerTest
{

    private static final String EXPECTED_OUTPUT = "https://www.midomain.example";
    private static final String SOURCE = "http://jdownloader.org/spielwiese";
    private static final String PASSWORDS = "password";

    @Test
    public void testClass() throws UnsupportedEncodingException, IOException, NoSuchMethodException
    {
        assertTrue(Modifier.isPublic(PlainTextHandler.class.getDeclaredConstructor(ConcurrentLinkedDeque.class).getModifiers()));
        assertEquals(PlainTextHandler.class.getSuperclass(), AbstractHandler.class);
    }

    @Test
    public void testConstructor() throws UnsupportedEncodingException, IOException, NoSuchMethodException
    {
        assertEquals(PlainTextHandler.class, new PlainTextHandler(new ConcurrentLinkedDeque<>()).getClass());
        assertThrows(IllegalArgumentException.class, () -> new PlainTextHandler(null));
    }

    @Test
    public void testHandlerPOST() throws UnsupportedEncodingException, IOException
    {
        String test1 = "urls=" + URLEncoder.encode(EXPECTED_OUTPUT, StandardCharsets.UTF_8.name()) + 
                "&passwords=" + URLEncoder.encode(PASSWORDS, StandardCharsets.UTF_8.name()) + 
                "&source=" + URLEncoder.encode(SOURCE, StandardCharsets.UTF_8.name());

        HttpExchange mockObject;
        ConcurrentLinkedDeque<Consumer<String>> listeners;
        Consumer<String> listener;
        PlainTextHandler handler;

        mockObject = mock(HttpExchange.class);
        when(mockObject.getRequestBody()).thenReturn(new ByteArrayInputStream(test1.getBytes()));
        when(mockObject.getRequestMethod()).thenReturn("POST");

        listeners = new ConcurrentLinkedDeque<>();
        listener = (String links) -> assertTrue(links.equals(EXPECTED_OUTPUT));
        listener = mock(Consumer.class, delegatesTo(listener));
        listeners.add(listener);

        handler = new PlainTextHandler(listeners);
        handler.handle(mockObject);
        verify(listener, times(1)).accept(EXPECTED_OUTPUT);

    }

    @Test
    public void testHandlerGET() throws UnsupportedEncodingException, IOException
    {
        String test1 = "urls=" + URLEncoder.encode(EXPECTED_OUTPUT, StandardCharsets.UTF_8.name()) + 
                "&passwords=" + URLEncoder.encode(PASSWORDS, StandardCharsets.UTF_8.name()) + 
                "&source=" + URLEncoder.encode(SOURCE, StandardCharsets.UTF_8.name());

        HttpExchange mockObject;
        ConcurrentLinkedDeque<Consumer<String>> listeners;
        Consumer<String> listener;
        PlainTextHandler handler;

        mockObject = mock(HttpExchange.class);
        when(mockObject.getRequestBody()).thenReturn(new ByteArrayInputStream(test1.getBytes()));
        when(mockObject.getRequestMethod()).thenReturn("GET");

        listeners = new ConcurrentLinkedDeque<>();
        listener = (String decrypted) -> assertTrue(decrypted.equals(EXPECTED_OUTPUT));
        listener = mock(Consumer.class, delegatesTo(listener));
        listeners.add(listener);

        handler = new PlainTextHandler(listeners);
        handler.handle(mockObject);
        verify(listener, times(0)).accept(Mockito.any(String.class));
    }


    @Test
    public void testHandlerOnMissingParameterTest() throws UnsupportedEncodingException, IOException
    {
        String test1 = "passwords=" + URLEncoder.encode(PASSWORDS, StandardCharsets.UTF_8.name()) + 
                "&source=" + URLEncoder.encode(SOURCE, StandardCharsets.UTF_8.name());
        
        HttpExchange mockObject;
        ConcurrentLinkedDeque<Consumer<String>> listeners;
        Consumer<String> listener;
        Consumer<Exception> onError;
        PlainTextHandler handler;

        mockObject = mock(HttpExchange.class);
        when(mockObject.getRequestBody()).thenReturn(new ByteArrayInputStream(test1.getBytes()));
        when(mockObject.getRequestMethod()).thenReturn("POST");

        listeners = new ConcurrentLinkedDeque<>();
        listener = (String decrypted) -> assertTrue(decrypted.equals(EXPECTED_OUTPUT));
        listener = mock(Consumer.class, delegatesTo(listener));
        listeners.add(listener);

        onError = (Exception ex) -> assertEquals(IOException.class, ex.getClass());
        onError = mock(Consumer.class, delegatesTo(onError));

        handler = new PlainTextHandler(listeners);
        handler.setOnError(onError);
        handler.handle(mockObject);

        verify(listener, times(0)).accept(EXPECTED_OUTPUT);
        verify(onError, times(1)).accept(Mockito.any(IOException.class));
    }

}
