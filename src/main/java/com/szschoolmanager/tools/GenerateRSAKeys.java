package com.szschoolmanager.tools;

import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

public class GenerateRSAKeys {
  public static void main(String[] args) throws Exception {
    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(2048);
    KeyPair pair = generator.generateKeyPair();
    PrivateKey privateKey = pair.getPrivate();
    PublicKey publicKey = pair.getPublic();

    try (FileOutputStream out = new FileOutputStream("src/main/resources/keys/private_key.pem")) {
      out.write(privateKey.getEncoded());
    }
    try (FileOutputStream out = new FileOutputStream("src/main/resources/keys/public_key.pem")) {
      out.write(publicKey.getEncoded());
    }

    System.out.println("✅ Clés RSA générées dans src/main/resources/keys/");
  }
}
