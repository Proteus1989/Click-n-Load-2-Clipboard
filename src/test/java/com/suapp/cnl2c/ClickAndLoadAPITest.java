/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suapp.cnl2c;

import com.suapp.cnl2c.cnl.AESDecrypter;
import com.suapp.cnl2c.cnl.ClickAndLoadAPI;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.BindException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Base64;
import java.util.function.Consumer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Antonio
 */
public class ClickAndLoadAPITest
{

    @Test
    public void testClass()
    {
        assertTrue(Modifier.isPublic(ClickAndLoadAPITest.class.getModifiers()));
    }

    @Test
    public void testConstructor() throws NoSuchMethodException
    {
        assertTrue(Modifier.isPrivate(ClickAndLoadAPI.class.getDeclaredConstructor().getModifiers()));
    }

    @Test
    public void testGetInstance() throws NoSuchMethodException
    {
        assertTrue(Modifier.isPublic(ClickAndLoadAPI.class.getDeclaredMethod("getInstance").getModifiers()));
        assertTrue(Modifier.isStatic(ClickAndLoadAPI.class.getDeclaredMethod("getInstance").getModifiers()));
        assertSame(ClickAndLoadAPI.getInstance(), ClickAndLoadAPI.getInstance());
    }

    @Test
    public void testRemoveListeners()
    {
        ClickAndLoadAPI api = ClickAndLoadAPI.getInstance();

        api.addListener(text -> System.out.println(text));
        assertTrue(api.getListeners().size() == 1);

        api.addListener(text -> System.out.println(text));
        assertTrue(api.getListeners().size() == 2);

        api.removeListener(text -> System.out.println(text));
        assertTrue(api.getListeners().size() == 2);

        Consumer<String> consumer = api.getListeners().remove(0);
        assertTrue(api.getListeners().size() == 2);

        api.removeListener(consumer);
        assertTrue(api.getListeners().size() == 1);

        api.getListeners().forEach(listener -> api.removeListener(listener));
        assertTrue(api.getListeners().isEmpty());
    }

    @Test
    public void testAddListeners()
    {
        assertThrows(IllegalArgumentException.class, () -> ClickAndLoadAPI.getInstance().addListener(null));

        ClickAndLoadAPI.getInstance().addListener(text -> System.out.println(text));
        assertTrue(ClickAndLoadAPI.getInstance().getListeners().size() == 1);

        ClickAndLoadAPI.getInstance().addListener(text -> System.out.println(text));
        assertTrue(ClickAndLoadAPI.getInstance().getListeners().size() == 2);

        ClickAndLoadAPI.getInstance().getListeners().forEach(listener -> ClickAndLoadAPI.getInstance().removeListener(listener));
    }

    @Test
    public void testStart() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InterruptedException
    {
        assertFalse(isPortAlreadyBinded());

        assertTrue(ClickAndLoadAPI.getInstance().startService());
        assertTrue(ClickAndLoadAPI.getInstance().isRunning());
        assertTrue(isPortAlreadyBinded());

        ClickAndLoadAPI.getInstance().stopService();
        Thread.sleep(500); // Waits a bit to ensure the port is closed 
    }

    @Test
    public void testStartPortAlreadyBinded() throws NoSuchFieldException, InstantiationException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, InterruptedException
    {
        assertTrue(ClickAndLoadAPI.getInstance().startService());
        assertTrue(ClickAndLoadAPI.getInstance().isRunning());
        assertTrue(isPortAlreadyBinded());

        Constructor<ClickAndLoadAPI> init = ClickAndLoadAPI.class.getDeclaredConstructor();
        init.setAccessible(true);
        ClickAndLoadAPI api = init.newInstance();

        api.startService();
        assertFalse(api.isRunning());

        ClickAndLoadAPI.getInstance().stopService();
        Thread.sleep(500); // Waits a bit to ensure the port is closed 
        assertFalse(isPortAlreadyBinded());
    }

    @Test
    public void testStop() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InterruptedException
    {
        assertTrue(ClickAndLoadAPI.getInstance().startService());
        assertTrue(ClickAndLoadAPI.getInstance().isRunning());
        assertTrue(isPortAlreadyBinded());
        ClickAndLoadAPI.getInstance().stopService();
        Thread.sleep(500); // Waits a bit to ensure the port is closed 
        assertFalse(isPortAlreadyBinded());
    }

    @Test
    public void testSetOnError() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InstantiationException, InvocationTargetException, InterruptedException
    {
        assertTrue(ClickAndLoadAPI.getInstance().startService());
        assertTrue(ClickAndLoadAPI.getInstance().isRunning());
        assertTrue(isPortAlreadyBinded());

        Constructor<ClickAndLoadAPI> init = ClickAndLoadAPI.class.getDeclaredConstructor();
        init.setAccessible(true);
        ClickAndLoadAPI api = init.newInstance();

        api.setOnError(e -> assertEquals(BindException.class, e.getClass()));
        api.startService();
        assertFalse(api.isRunning());

        ClickAndLoadAPI.getInstance().stopService();
        Thread.sleep(500); // Waits a bit to ensure the port is closed 
        assertFalse(isPortAlreadyBinded());
    }

    @Test
    public void testDecryptBase64() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException
    {
        String cryptedBase64 = "0vx3+MUms2VQvrbKLuPVOcnxbpYFpEqRnv9cFBgDltQ=";
        String decrypted = "Decrypted string";

        String jk = "12345678901234567890251436587951";
        String jk2 = "123456789012345678902514365879511";
        String jk3 = "1234567890123456789025143658795112";
        String jk4 = "1234567890123456789025143658795";
        String jk5 = "123456789012345678902514365879";

        ClickAndLoadAPI.getInstance().addListener(link -> assertTrue(link.equals(decrypted)));

        assertEquals(decrypted, ClickAndLoadAPI.getInstance().decrypt(cryptedBase64, jk));
        assertThrows(IllegalArgumentException.class, () -> ClickAndLoadAPI.getInstance().decrypt(cryptedBase64, jk2));
        assertThrows(IllegalArgumentException.class, () -> ClickAndLoadAPI.getInstance().decrypt(cryptedBase64, jk3));
        assertThrows(IllegalArgumentException.class, () -> ClickAndLoadAPI.getInstance().decrypt(cryptedBase64, jk4));
        assertThrows(IllegalArgumentException.class, () -> ClickAndLoadAPI.getInstance().decrypt(cryptedBase64, jk5));

        ClickAndLoadAPI.getInstance().getListeners().forEach(l -> ClickAndLoadAPI.getInstance().removeListener(l));

    }

    @Test
    public void testDecryptBinray() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException
    {
        String cryptedBase64 = "0vx3+MUms2VQvrbKLuPVOcnxbpYFpEqRnv9cFBgDltQ=";
        String decrypted = "Decrypted string";

        String jk = "12345678901234567890251436587951";

        byte[] cryptedBinary = Base64.getDecoder().decode(cryptedBase64);
        Method method = AESDecrypter.class.getDeclaredMethod("hexToByteArray", String.class);
        method.setAccessible(true);

        byte[] jkBinary = (byte[]) method.invoke(null, jk);

        ClickAndLoadAPI.getInstance().addListener(link -> assertTrue(link.equals(decrypted)));

        assertEquals(decrypted, ClickAndLoadAPI.getInstance().decrypt(cryptedBinary, jkBinary));
        assertThrows(RuntimeException.class, () -> ClickAndLoadAPI.getInstance().decrypt(cryptedBinary, Arrays.copyOf(jkBinary, jkBinary.length - 1)));
        assertThrows(RuntimeException.class, () -> ClickAndLoadAPI.getInstance().decrypt(cryptedBinary, Arrays.copyOf(jkBinary, jkBinary.length - 2)));
        
        ClickAndLoadAPI.getInstance().getListeners().forEach(l -> ClickAndLoadAPI.getInstance().removeListener(l));

    }

    public boolean isPortAlreadyBinded() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        Field field = ClickAndLoadAPI.class.getDeclaredField("CLICK_AND_LOAD_PORT");
        field.setAccessible(true);
        try (Socket ignored = new Socket("localhost", field.getInt(null)))
        {
            return true;
        } catch (IOException ignored)
        {
            return false;
        }
    }
}
