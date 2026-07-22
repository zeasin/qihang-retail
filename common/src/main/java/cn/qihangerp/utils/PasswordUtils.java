package cn.qihangerp.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

public class PasswordUtils {
    // 生成随机盐值
    public static String generateSalt() {
        byte[] salt = new byte[16];
        new Random().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);  // 将盐转换为Base64字符串，方便存储
    }
    // MD5 哈希
    public static String hashPasswordWithSalt(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            String combined = password + salt;  // 将密码和盐拼接
            byte[] hashBytes = md.digest(combined.getBytes());  // 进行哈希处理
            return bytesToHex(hashBytes);  // 将字节数组转换为16进制字符串
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 将字节数组转换为16进制字符串
    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

}
