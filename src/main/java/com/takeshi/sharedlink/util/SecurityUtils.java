package com.takeshi.sharedlink.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Objects;

public class SecurityUtils {

    private static final String ALGORITHM = "AES";

    private static final String KEY = "M/6vZf2UjP9nQZ7R1HjZel+yFf1VY2X=";

    public static String encrypt(String value) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(value.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String value) throws Exception {
        if (Objects.isNull(value) || value.isBlank()) {
            return "";
        }
        SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decoded = Base64.getDecoder().decode(value);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted);
    }

}
