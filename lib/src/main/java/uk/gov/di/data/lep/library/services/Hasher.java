package uk.gov.di.data.lep.library.services;

import uk.gov.di.data.lep.library.exceptions.HashingException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hasher {
    private Hasher() {
        throw new IllegalStateException("Utility class");
    }

    public static String hash(String dataToHash) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var hashInBytes = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
            var hexString = new StringBuilder();
            for (var hashByte : hashInBytes) {
                var hex = Integer.toHexString(0xff & hashByte);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new HashingException(ex);
        }
    }
}
