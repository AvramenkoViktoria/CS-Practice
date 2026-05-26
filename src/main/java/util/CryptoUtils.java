package util;

import io.github.cdimascio.dotenv.Dotenv;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils {

    private static final Dotenv dotenv = Dotenv.load();

    private static final String ALGORITHM = dotenv.get("CRYPTO_ALGORITHM", "AES");
    private static final String KEY_STRING = dotenv.get("CRYPTO_KEY");

    private static final byte[] KEY;

    static {
        if (KEY_STRING == null || KEY_STRING.isBlank())
            throw new RuntimeException("CRYPTO_KEY is not set in .env file!");
        KEY = KEY_STRING.getBytes();
    }

    public static byte[] encrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(KEY, ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        return cipher.doFinal(data);
    }

    public static byte[] decrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(KEY, ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        return cipher.doFinal(data);
    }
}