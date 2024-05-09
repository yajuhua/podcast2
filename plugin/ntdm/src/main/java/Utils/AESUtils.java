package Utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES加密128位CBC模式工具类
 */
public class AESUtils {

    //解密密钥(这个是ntdm9动漫的)
    public static final String KEY = "57A891D97E332A9D";//密钥key
    public static  String IV;//向量iv

    //认证密钥(自行随机生成)
    public static final String AK = "s2ip9g3y3bjr5zz7ws6kjgx3ysr82zzw";//AccessKey
    public static final String SK = "uv8zr0uen7aim8m7umcuooqzdv8cbvtf";//SecretKey

    //加密
    public static String Encrypt(String content) throws Exception {
        byte[] raw = KEY.getBytes("utf-8");
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");//"算法/模式/补码方式"
        //使用CBC模式，需要一个向量iv，可增加加密算法的强度
        IvParameterSpec ips = new IvParameterSpec(IV.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ips);
        byte[] encrypted = cipher.doFinal(content.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    //解密
    public static String Decrypt(String content) throws Exception {
        try {
            byte[] raw = KEY.getBytes("utf-8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ips = new IvParameterSpec(IV.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ips);
            byte[] encrypted1 = Base64.getDecoder().decode(content);
            try {
                byte[] original = cipher.doFinal(encrypted1);
                String originalString = new String(original);
                return originalString;
            } catch (Exception e) {
                System.out.println(e.toString());
                return null;
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
            return null;
        }
    }

    //解密
    public static String Decrypt(String content,String IV) throws Exception {
        try {
            byte[] raw = KEY.getBytes("utf-8");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ips = new IvParameterSpec(IV.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ips);
            byte[] encrypted1 = Base64.getDecoder().decode(content);
            try {
                byte[] original = cipher.doFinal(encrypted1);
                String originalString = new String(original);
                return originalString;
            } catch (Exception e) {
                System.out.println(e.toString());
                return null;
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
            return null;
        }
    }

    //获取认证签名(身份认证需要)
    public static String getSign(String currentTime) throws Exception {
        String sign = "";
        Map<String, Object> map=new HashMap<String,Object>();
        map.put("ak",AK);
        map.put("sk",SK);
        map.put("ts",currentTime);
        //获取 参数字典排序后字符串
        String decrypt = getOrderMap(map);
        try {
            //指定sha1算法
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(decrypt.getBytes());
            //获取字节数组
            byte messageDigest[] = digest.digest();
            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            // 字节数组转换为十六进制数
            for (int i = 0; i < messageDigest.length; i++) {
                String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            sign = hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sign;
    }

    //获取参数的字典排序
    private static String getOrderMap(Map<String,Object> maps){
        List<String> paramNames = new ArrayList<String>();
        for(Map.Entry<String,Object> entry : maps.entrySet()){
            paramNames.add(entry.getValue().toString());
        }
        Collections.sort(paramNames);
        StringBuilder paramStr = new StringBuilder();
        for(String paramName : paramNames){
            paramStr.append(paramName);
        }
        return paramStr.toString();
    }

}

