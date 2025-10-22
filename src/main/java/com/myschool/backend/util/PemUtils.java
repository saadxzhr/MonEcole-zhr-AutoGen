package com.myschool.backend.util;

import java.io.BufferedReader;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.stream.Collectors;

public class PemUtils {

    private static String stripPemHeader(String pem, String beginMarker, String endMarker) {
        try (BufferedReader reader = new BufferedReader(new StringReader(pem))) {
            return reader.lines()
                    .filter(line -> !line.contains(beginMarker) && !line.contains(endMarker))
                    .collect(Collectors.joining());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lecture du contenu PEM", e);
        }
    }

    public static PrivateKey parsePrivateKeyFromPem(String pem) {
        try {
            String cleanPem = stripPemHeader(pem, "-----BEGIN PRIVATE KEY", "-----END PRIVATE KEY");
            byte[] decoded = Base64.getDecoder().decode(cleanPem);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lecture clé privée RSA", e);
        }
    }

    public static PublicKey parsePublicKeyFromPem(String pem) {
        try {
            String cleanPem = stripPemHeader(pem, "-----BEGIN PUBLIC KEY", "-----END PUBLIC KEY");
            byte[] decoded = Base64.getDecoder().decode(cleanPem);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lecture clé publique RSA", e);
        }
    }
}
