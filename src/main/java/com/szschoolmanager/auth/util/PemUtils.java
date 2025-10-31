package com.szschoolmanager.auth.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.stream.Collectors;

public final class PemUtils {

    // 🔒 Constructeur privé pour empêcher l’instanciation (S1118)
    private PemUtils() {
        throw new UnsupportedOperationException("Utility class - cannot be instantiated");
    }

    private static String stripPemHeader(String pem, String beginMarker, String endMarker)
            throws IOException {
        try (BufferedReader reader = new BufferedReader(new StringReader(pem))) {
            return reader
                    .lines()
                    .filter(line -> !line.contains(beginMarker) && !line.contains(endMarker))
                    .collect(Collectors.joining());
        }
    }

    public static PrivateKey parsePrivateKeyFromPem(String pem)
            throws IOException, GeneralSecurityException {
        try {
            String cleanPem = stripPemHeader(pem, "-----BEGIN PRIVATE KEY", "-----END PRIVATE KEY");
            byte[] decoded = Base64.getDecoder().decode(cleanPem);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(keySpec);
        } catch (InvalidKeySpecException e) {
            throw new GeneralSecurityException("Invalid RSA private key format", e);
        }
    }

    public static PublicKey parsePublicKeyFromPem(String pem)
            throws IOException, GeneralSecurityException {
        try {
            String cleanPem = stripPemHeader(pem, "-----BEGIN PUBLIC KEY", "-----END PUBLIC KEY");
            byte[] decoded = Base64.getDecoder().decode(cleanPem);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(keySpec);
        } catch (InvalidKeySpecException e) {
            throw new GeneralSecurityException("Invalid RSA public key format", e);
        }
    }
}
