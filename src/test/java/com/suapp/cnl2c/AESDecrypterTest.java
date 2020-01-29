/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suapp.cnl2c;

import com.suapp.cnl2c.cnl.AESDecrypter;
import com.suapp.cnl2c.utils.TestUtilities;
import static com.suapp.cnl2c.utils.TestUtilities.catchCauseException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Antonio
 */
public class AESDecrypterTest
{

    private static final String KEY = "12345678901234567890251436587951"; // Must be 16 bytes long
    private static final String EXACT_LENGTH_BODY = "Decrypted string"; // 16 bytes test

    @Test
    public void testClass() throws UnsupportedEncodingException, IOException, NoSuchMethodException
    {
        assertTrue(Modifier.isPublic(AESDecrypter.class.getDeclaredConstructor().getModifiers()));
    }

    @Test
    public void testConstructor() throws UnsupportedEncodingException, IOException, NoSuchMethodException
    {
        assertEquals(AESDecrypter.class, new AESDecrypter().getClass());
    }

    @Test
    public void testDecrypt()
    {
        String smallerLengthBody = "Decrypted"; // 9 bytes test
        String biggerLengthBody = "Decrypted string!"; // 17 bytes test

        assertEquals(EXACT_LENGTH_BODY, AESDecrypter.decrypt(encript(EXACT_LENGTH_BODY, KEY), KEY));
        assertEquals(smallerLengthBody, AESDecrypter.decrypt(encript(smallerLengthBody, KEY), KEY));
        assertEquals(biggerLengthBody, AESDecrypter.decrypt(encript(biggerLengthBody, KEY), KEY));

    }

    @Test
    public void testDecryptOnInvalidKey()
    {
        assertThrows(IllegalArgumentException.class, () -> AESDecrypter.decrypt(encript(EXACT_LENGTH_BODY, KEY), KEY.substring(1))); // Invalid key length
        assertThrows(IllegalArgumentException.class, () -> AESDecrypter.decrypt(encript(EXACT_LENGTH_BODY, KEY), KEY.substring(2))); // Invalid key length
        assertThrows(IllegalArgumentException.class, () -> AESDecrypter.decrypt(encript(EXACT_LENGTH_BODY, KEY), KEY + "1")); // Invalid key length
        assertThrows(IllegalArgumentException.class, () -> AESDecrypter.decrypt(encript(EXACT_LENGTH_BODY, KEY), KEY + "12")); // Invalid key length
    }

    @Test
    public void testDecryptOnWrongKey()
    {
        assertNotEquals(EXACT_LENGTH_BODY, AESDecrypter.decrypt(encript(EXACT_LENGTH_BODY, KEY), "12121212121212121212121212121212")); // Wrong key
        assertNotEquals(EXACT_LENGTH_BODY, AESDecrypter.decrypt(encript(EXACT_LENGTH_BODY, KEY), "99999999999999999654987654654987")); // Wrong key
    }

    @Test
    public void testDecryptOnMissingParameter()
    {
        assertThrows(IllegalArgumentException.class, () -> AESDecrypter.decrypt(encript(EXACT_LENGTH_BODY, KEY), null)); // Null key
        assertThrows(IllegalArgumentException.class, () -> AESDecrypter.decrypt(null, KEY)); // Null body
    }

    private String encript(String body, String key)
    {
        try
        {
            body = rightPadBasedOnKeyLength(body, key);
            IvParameterSpec ivSpec = new IvParameterSpec(DatatypeConverter.parseHexBinary(key));
            SecretKeySpec skeySpec = new SecretKeySpec(DatatypeConverter.parseHexBinary(key), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(body.getBytes()));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex)
        {
            throw new RuntimeException("Error encripting links", ex);
        }
    }

    @Test
    public void testHexToByteArray() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
    {
        Method method = AESDecrypter.class.getDeclaredMethod("hexToByteArray", String.class);
        method.setAccessible(true);
        assertTrue(((byte[]) method.invoke(null, KEY)).length == 16);
        assertTrue(((byte[]) method.invoke(null, KEY.substring(2))).length == 15);
        assertTrue(((byte[]) method.invoke(null, KEY + "94")).length == 17);
        
        assertThrows(IllegalArgumentException.class, () -> catchCauseException(method, KEY.substring(1))); // Odd length
        assertThrows(IllegalArgumentException.class, () -> catchCauseException(method, new Object[]{ null })); // null value
    }

    private String rightPadBasedOnKeyLength(String body, String key)
    {
        while (body.length() % key.length() != 0)
            body = body + " ";
        return body;
    }
}
