package org.biometric.provider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

public class KeyPair {
	private static final String ALGORITHM = "RSA";
	private static final int KEY_LENGTH = 2048;

	@SuppressWarnings({ "java:S1075" })
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
		KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance(ALGORITHM);
		keyGenerator.initialize(KEY_LENGTH, new SecureRandom());
		java.security.KeyPair keypair = keyGenerator.generateKeyPair();

		FileWriter fileWriter = new FileWriter(new File("C:\\Users\\m1048290\\Desktop\\keys\\private.pem"));
		try (PemWriter writer = new PemWriter(fileWriter)) {
			writer.writeObject(new PemObject("Private Key", keypair.getPrivate().getEncoded()));
		}

		fileWriter = new FileWriter(new File("C:\\Users\\m1048290\\Desktop\\keys\\public.pem"));
		try (PemWriter writer = new PemWriter(fileWriter)) {
			writer.writeObject(new PemObject("Public Key", keypair.getPublic().getEncoded()));
		}
	}
}