package io.github.yajuhua.podcast2.common.utils;

import io.github.yajuhua.podcast2.common.constant.MessageConstant;
import io.github.yajuhua.podcast2.common.exception.CertificateFileException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 证书和密钥验证格式是否正确
 */
public class CertUtils {

    /**
     * 判断是否是crt证书文件
     * @param inputStream
     * @return
     * @throws Exception
     */
    public static boolean isCertFile(InputStream inputStream){
        try {
            CertificateFactory.getInstance("X.509").generateCertificate(inputStream);
            return true;
        } catch (CertificateException e) {
            throw new CertificateFileException(MessageConstant.CRT_FILE_ERROR);
        }
    }

    /**
     * 判断是否是密钥文件
     * @param inputStream
     * @return
     */
    public static boolean isKeyFile(InputStream inputStream){
        try {
            //提取密钥
            List<String> keyLines = IOUtils.readLines(inputStream, "utf-8");
            String privateKeyContent =  keyLines.stream().filter(s -> !s.contains("BEGIN PRIVATE KEY")
                            && !s.contains("END PRIVATE KEY")).map(String::trim).collect(Collectors.joining());

            //解码
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA"); // 或其他算法，根据实际情况调整
            keyFactory.generatePrivate(keySpec);
            return true;
        } catch (Exception e) {
            throw new CertificateFileException(MessageConstant.KEY_FILE_ERROR);
        }
    }

    /**
     *拷贝流,inputstream流只能读一次
     * @param input
     * @return
     */
    public static ByteArrayOutputStream cloneInputStream(InputStream input) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = input.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            return baos;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
