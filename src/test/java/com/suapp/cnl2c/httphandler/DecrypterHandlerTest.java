/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suapp.cnl2c.httphandler;

import com.suapp.cnl2c.cnl.ClickAndLoadAPI;
import com.suapp.cnl2c.cnl.httphandler.DecrypterHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import static org.mockito.AdditionalAnswers.delegatesTo;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @author Antonio
 */
public class DecrypterHandlerTest
{

    private static final String CRYPTED = "0vx3+MUms2VQvrbKLuPVOcnxbpYFpEqRnv9cFBgDltQ=";
    private static final String JK = "function f(){ return '12345678901234567890251436587951';}";
    private static final String EXPECTED_OUTPUT = "Decrypted string";
    private static final String INVALID_KEY = "Invalid key";

    @Test
    public void testClass() throws UnsupportedEncodingException, IOException, NoSuchMethodException
    {
        assertTrue(Modifier.isPublic(DecrypterHandler.class.getDeclaredConstructor(ConcurrentLinkedDeque.class).getModifiers()));
    }

    @Test
    public void testConstructor() throws UnsupportedEncodingException, IOException, NoSuchMethodException
    {
        assertEquals(DecrypterHandler.class, new DecrypterHandler(new ConcurrentLinkedDeque<>()).getClass());
        assertThrows(IllegalArgumentException.class, () -> new DecrypterHandler(null));
    }

    @Test
    public void testHandlerPOST() throws UnsupportedEncodingException, IOException
    {
        String test1 = "jk=" + URLEncoder.encode(JK, StandardCharsets.UTF_8.name()) + "&crypted=" + URLEncoder.encode(CRYPTED, StandardCharsets.UTF_8.name());

        HttpExchange mockObject;
        ConcurrentLinkedDeque<Consumer<String>> listeners;
        Consumer<String> listener;
        DecrypterHandler handler;

        mockObject = mock(HttpExchange.class);
        when(mockObject.getRequestBody()).thenReturn(new ByteArrayInputStream(test1.getBytes()));
        when(mockObject.getRequestMethod()).thenReturn("POST");

        listeners = new ConcurrentLinkedDeque<>();
        listener = (String decrypted) -> assertTrue(decrypted.equals(EXPECTED_OUTPUT));
        listener = mock(Consumer.class, delegatesTo(listener));
        listeners.add(listener);

        handler = new DecrypterHandler(listeners);
        handler.handle(mockObject);
        verify(listener, times(1)).accept(EXPECTED_OUTPUT);

        String test2 = "crypted=" + URLEncoder.encode(CRYPTED, StandardCharsets.UTF_8.name()) + "&jk=" + URLEncoder.encode(JK, StandardCharsets.UTF_8.name());

        mockObject = mock(HttpExchange.class);
        when(mockObject.getRequestBody()).thenReturn(new ByteArrayInputStream(test2.getBytes()));
        when(mockObject.getRequestMethod()).thenReturn("POST");

        listeners = new ConcurrentLinkedDeque<>();
        listener = (String decrypted) -> assertTrue(decrypted.equals(EXPECTED_OUTPUT));
        listener = mock(Consumer.class, delegatesTo(listener));
        listeners.add(listener);

        handler = new DecrypterHandler(listeners);
        handler.handle(mockObject);
        verify(listener, times(1)).accept(EXPECTED_OUTPUT);

        String test3 = "crypted=" + URLEncoder.encode(INVALID_KEY, StandardCharsets.UTF_8.name()) + "&jk=" + URLEncoder.encode(JK, StandardCharsets.UTF_8.name());

        mockObject = mock(HttpExchange.class);
        when(mockObject.getRequestBody()).thenReturn(new ByteArrayInputStream(test3.getBytes()));
        when(mockObject.getRequestMethod()).thenReturn("POST");

        listeners = new ConcurrentLinkedDeque<>();
        listener = (String decrypted) -> assertTrue(decrypted.equals(EXPECTED_OUTPUT));
        listener = mock(Consumer.class, delegatesTo(listener));
        listeners.add(listener);

        handler = new DecrypterHandler(listeners);
        handler.handle(mockObject);
        verify(listener, times(0)).accept(EXPECTED_OUTPUT);

    }

    @Test
    public void testHandlerGET() throws UnsupportedEncodingException, IOException
    {
        String test1 = "jk=" + URLEncoder.encode(JK, StandardCharsets.UTF_8.name()) + "&crypted=" + URLEncoder.encode(CRYPTED, StandardCharsets.UTF_8.name());

        HttpExchange mockObject;
        ConcurrentLinkedDeque<Consumer<String>> listeners;
        Consumer<String> listener;
        DecrypterHandler handler;

        mockObject = mock(HttpExchange.class);
        when(mockObject.getRequestBody()).thenReturn(new ByteArrayInputStream(test1.getBytes()));
        when(mockObject.getRequestMethod()).thenReturn("GET");

        listeners = new ConcurrentLinkedDeque<>();
        listener = (String decrypted) -> assertTrue(decrypted.equals(EXPECTED_OUTPUT));
        listener = mock(Consumer.class, delegatesTo(listener));
        listeners.add(listener);

        handler = new DecrypterHandler(listeners);
        handler.handle(mockObject);
        verify(listener, times(0)).accept(EXPECTED_OUTPUT);
    }

    @Test
    public void testHandlerOnMissingParameter() throws UnsupportedEncodingException, IOException
    {
        testOnErrorUtil("jk=" + URLEncoder.encode(JK, StandardCharsets.UTF_8.name()));
        testOnErrorUtil("crypted=" + URLEncoder.encode(CRYPTED, StandardCharsets.UTF_8.name()));
    }

    private void testOnErrorUtil(String test2) throws IOException
    {
        HttpExchange mockObject;
        ConcurrentLinkedDeque<Consumer<String>> listeners;
        Consumer<String> listener;
        Consumer<Exception> onError;
        DecrypterHandler handler;

        mockObject = mock(HttpExchange.class);
        when(mockObject.getRequestBody()).thenReturn(new ByteArrayInputStream(test2.getBytes()));
        when(mockObject.getRequestMethod()).thenReturn("POST");

        listeners = new ConcurrentLinkedDeque<>();
        listener = (String decrypted) -> assertTrue(decrypted.equals(EXPECTED_OUTPUT));
        listener = mock(Consumer.class, delegatesTo(listener));
        listeners.add(listener);

        onError = (Exception ex) -> assertEquals(IOException.class, ex.getClass());
        onError = mock(Consumer.class, delegatesTo(onError));

        handler = new DecrypterHandler(listeners);
        handler.setOnError(onError);
        handler.handle(mockObject);

        verify(listener, times(0)).accept(EXPECTED_OUTPUT);
        verify(onError, times(1)).accept(Mockito.any(IOException.class));
    }

    @Test
    public void testSetOnError() throws UnsupportedEncodingException, IOException
    {
        String test1 = "jk=" + URLEncoder.encode(INVALID_KEY, StandardCharsets.UTF_8.name()) + "&crypted=" + URLEncoder.encode(CRYPTED, StandardCharsets.UTF_8.name());

        HttpExchange mockObject;
        ConcurrentLinkedDeque<Consumer<String>> listeners;
        DecrypterHandler handler;
        Consumer<String> listener;
        Consumer<Exception> onError;

        mockObject = mock(HttpExchange.class);
        when(mockObject.getRequestBody()).thenReturn(new ByteArrayInputStream(test1.getBytes()));
        when(mockObject.getRequestMethod()).thenReturn("POST");

        listeners = new ConcurrentLinkedDeque<>();

        onError = (Exception ex) -> assertEquals(IOException.class, ex.getClass());
        onError = mock(Consumer.class, delegatesTo(onError));

        handler = new DecrypterHandler(listeners);
        handler.setOnError(onError);
        handler.handle(mockObject);
        verify(onError, times(1)).accept(Mockito.any());

        String wrongKey = "function f(){ return '11111111111134567890251436587951';}";
        String test2 = "jk=" + URLEncoder.encode(wrongKey, StandardCharsets.UTF_8.name()) + "&crypted=" + URLEncoder.encode(CRYPTED, StandardCharsets.UTF_8.name());

        mockObject = mock(HttpExchange.class);
        when(mockObject.getRequestBody()).thenReturn(new ByteArrayInputStream(test2.getBytes()));
        when(mockObject.getRequestMethod()).thenReturn("POST");

        listeners = new ConcurrentLinkedDeque<>();
        listener = (String decrypted) -> assertFalse(decrypted.equals(EXPECTED_OUTPUT));
        listener = mock(Consumer.class, delegatesTo(listener));
        listeners.add(listener);

        onError = (Exception ex) -> assertEquals(IOException.class, ex.getClass());
        onError = mock(Consumer.class, delegatesTo(onError));

        handler = new DecrypterHandler(listeners);
        handler.setOnError(onError);
        handler.handle(mockObject);
        verify(onError, times(0)).accept(Mockito.any());
        verify(listener, times(1)).accept(Mockito.anyString());
    }

}
