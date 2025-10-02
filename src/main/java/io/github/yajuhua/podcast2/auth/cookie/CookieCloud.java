package io.github.yajuhua.podcast2.auth.cookie;

import org.springframework.util.DigestUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * https://github.com/easychen/CookieCloud/
 */
public class CookieCloud {
    /**
     * 根据密码和盐值生成密钥和 IV
     * @param keyLen
     * @param ivLen
     * @param salt
     * @param password
     * @return
     * @throws Exception
     */
    private static byte[] EVP_BytesToKey(int keyLen, int ivLen, byte[] salt, byte[] password) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] key = new byte[keyLen + ivLen];
        byte[] prev = new byte[0];
        int generated = 0;
        while (generated < key.length) {
            md.reset();
            md.update(prev);
            md.update(password);
            if (salt != null) md.update(salt);
            prev = md.digest();
            int toCopy = Math.min(prev.length, key.length - generated);
            System.arraycopy(prev, 0, key, generated, toCopy);
            generated += toCopy;
        }
        return key;
    }

    /**
     * 解密带有盐值的
     * @param base64CipherText
     * @param password
     * @return
     * @throws Exception
     */
    private static String decrypt(String base64CipherText, String password) throws Exception {
        byte[] cipherData = Base64.getDecoder().decode(base64CipherText);
        byte[] salt = null;
        int offset = 0;

        // 提取盐值
        if (cipherData.length > 16 && new String(cipherData, 0, 8, StandardCharsets.US_ASCII).equals("Salted__")) {
            salt = Arrays.copyOfRange(cipherData, 8, 16);
            offset = 16;
        }

        // 根据密码和盐值生成密钥和 IV
        byte[] keyIv = EVP_BytesToKey(32, 16, salt, password.getBytes(StandardCharsets.UTF_8));
        SecretKeySpec key = new SecretKeySpec(Arrays.copyOfRange(keyIv, 0, 32), "AES");
        IvParameterSpec iv = new IvParameterSpec(Arrays.copyOfRange(keyIv, 32, 48));

        // 使用 AES/CBC/PKCS5Padding 解密
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] decrypted = cipher.doFinal(cipherData, offset, cipherData.length - offset);

        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * 解密
     * @param base64CipherText
     * @param uuid
     * @param password
     * @return
     * @throws Exception
     */
    public static String decrypt(String base64CipherText, String uuid, String password) throws Exception {
        String key = DigestUtils.md5DigestAsHex((uuid + '-' + password).getBytes(StandardCharsets.UTF_8)).substring(0,16);
        return decrypt(base64CipherText, key);
    }
}
