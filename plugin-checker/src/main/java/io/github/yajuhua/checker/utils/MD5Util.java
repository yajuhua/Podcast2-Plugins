package io.github.yajuhua.checker.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class MD5Util {

    public static String getFileMD5(File file) {
        try (InputStream fis = new FileInputStream(file)) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[4096];
            int numRead;

            while ((numRead = fis.read(buffer)) != -1) {
                md.update(buffer, 0, numRead);
            }

            byte[] digest = md.digest();
            return bytesToHex(digest);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexStr = new StringBuilder();
        for (byte b : bytes) {
            // & 0xff 保证是正数，然后转成两位十六进制字符串
            String hex = Integer.toHexString(b & 0xff);
            if (hex.length() == 1) hexStr.append("0");
            hexStr.append(hex);
        }
        return hexStr.toString();
    }
}

