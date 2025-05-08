package org.biometric.provider;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.*;

class KeyPairTest {
    private static final String PRIVATE_KEY_PATH = "C:\\Users\\m1048290\\Desktop\\keys\\private.pem";
    private static final String PUBLIC_KEY_PATH = "C:\\Users\\m1048290\\Desktop\\keys\\public.pem";
    private static final String KEYS_DIR = "C:\\Users\\m1048290\\Desktop\\keys";

    // Sets up the keys directory and deletes any existing key files before each test.
    @BeforeEach
    void setup() {
        File keyDir = new File(KEYS_DIR);
        if (!keyDir.exists()) {
            keyDir.mkdirs();
        }
        new File(PRIVATE_KEY_PATH).delete();
        new File(PUBLIC_KEY_PATH).delete();
    }

    // Tests that an RSA key pair is generated successfully and validates algorithm names.
    @Test
    void testGenerateRSAKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        assertNotNull(publicKey, "Public key should not be null");
        assertNotNull(privateKey, "Private key should not be null");
        assertEquals("RSA", publicKey.getAlgorithm(), "Algorithm should be RSA");
        assertEquals("RSA", privateKey.getAlgorithm(), "Algorithm should be RSA");
    }

    // Tests that the generated public and private keys are different.
    @Test
    void testKeysAreDifferent() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        assertNotEquals(
                keyPair.getPublic().getEncoded(),
                keyPair.getPrivate().getEncoded(),
                "Public and private keys should not be the same"
        );
    }

    // Cleans up the key files after each test.
    @AfterEach
    void cleanupAfter() {
        new File(PRIVATE_KEY_PATH).delete();
        new File(PUBLIC_KEY_PATH).delete();
    }
}