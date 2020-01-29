package com.suapp.cnl2c.cnl;

import java.io.UnsupportedEncodingException;
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

/**
 * An AES decrypter utility used to decrypt Click'n Load data
 *
 * @author Antonio
 */
public class AESDecrypter
{

    /**
     * Given crypted content (cypted) and its key (jk) this method will return
     * decrypted data.
     *
     * @param crypted Actual crypted content
     * @param jk Decryption key
     * @return Decrypted data
     */
    public static String decrypt(String crypted, final String jk)
    {
        if (jk == null)
            throw new IllegalArgumentException("jk cannot be null");
        if (jk.length() % 2 != 0)
            throw new IllegalArgumentException("jk cannot have an odd length");
        if (crypted == null)
            throw new IllegalArgumentException("crypted cannot be null");

        byte[] key = hexToByteArray(jk);
        if (key.length != 16)
            throw new IllegalArgumentException("jk must be 16 bytes key");
        byte[] baseDecoded = Base64.getDecoder().decode(crypted.trim().replaceAll("\\s", "+"));
        String ret = decrypt(baseDecoded, key);
        return ret.trim();
    }

    /**
     * Decrypt data using AES/CBC/NoPadding Cipher.
     *
     * @param b Base64 decoded crypted data
     * @param key ByteArray of original hexadecimal key
     * @return Decrypted data
     */
    public static String decrypt(byte[] b, byte[] key)
    {
        try
        {
            IvParameterSpec ivSpec = new IvParameterSpec(key);
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
            return new String(cipher.doFinal(b), "UTF-8");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException ex)
        {
            throw new RuntimeException("Error decrypting links", ex);
        }
    }

    /**
     * Convert an hexadecimal string to a byte array.
     * {@link javax.xml.bind.DatatypeConverter#parseHexBinary DatatypeConverter.parseHexBinary}
     * has a similar behavior, but this class is only available in JDK, not in
     * JRE.
     *
     * @param s Hexadecimal string to be converted. String length must be an
     * even number.
     * @return Hexadecimal string converted to a byte array
     * @throws IllegalArgumentException This method will thrown an exception if
     * hexString is null or hexString length is odd
     */
    private static byte[] hexToByteArray(final String hexString) throws IllegalArgumentException
    {
        if (hexString == null)
            throw new IllegalArgumentException("hexString cannot be null");
        if (hexString.length() % 2 != 0)
            throw new IllegalArgumentException("hexString length must be an even number");
        final int len = hexString.length();
        final byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character.digit(hexString.charAt(i + 1), 16));
        return data;
    }

}
