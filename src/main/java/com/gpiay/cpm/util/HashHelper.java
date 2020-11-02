package com.gpiay.cpm.util;

import com.gpiay.cpm.CPMMod;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashHelper {
    public static MessageDigest MD;

    static {
        try {
            MD = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            CPMMod.LOGGER.error(e);
        }
    }

    public static String hashCode(InputStream is) throws IOException {
        byte[] buffer = new byte[1024];
        int length = -1;

        MD.reset();
        while ((length = is.read(buffer, 0, 1024)) != -1) {
            MD.update(buffer, 0, length);
        }

        is.close();

        byte[] md5Bytes  = MD.digest();
        BigInteger bigInt = new BigInteger(1, md5Bytes);
        return bigInt.toString(16);
    }

    public static String hashCode(File file) throws IOException {
        return hashCode(new BufferedInputStream(new FileInputStream(file)));
    }

    public static String hashCode(byte[] data) throws IOException {
        return hashCode(new ByteArrayInputStream(data));
    }
}
