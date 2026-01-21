package com.example.bankcards.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
@Slf4j
public class CardEncryptionUtil {

    @Value("${app:security:encryption-key}")
    private String encryptionKey;

    public String encryptPan(String pan) {
        try {
            Cipher chiper = Cipher.getInstance("AES");

            SecretKey key = new SecretKeySpec(
                    encryptionKey.getBytes(),
                    0,
                    32,
                    "AES"
            );

            chiper.init(Cipher.ENCRYPT_MODE, key);

            byte[] encryptedBytes = chiper.doFinal(pan.getBytes());

            return Base64.getEncoder().encodeToString(encryptedBytes);

        } catch (Exception e) {
            log.error("Error encrypting PAN", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decryptPan(String encryptedPan) {
        try {
            Cipher cipher = Cipher.getInstance("AES");

            SecretKey key = new SecretKeySpec(
                    encryptionKey.getBytes(), 0, 32, "AES"
            );

            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] decodedBytes = Base64.getDecoder().decode(encryptedPan);

            byte[] decryptedBytes = cipher.doFinal(decodedBytes);

            return new String(decryptedBytes);

        } catch (Exception e) {
            log.error("Error decrypting PAN", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }

    public String hashPan(String pan) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pan.getBytes());
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("Error hashing PAN", e);
            throw new RuntimeException("Hash failed", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
