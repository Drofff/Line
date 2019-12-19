package drofff.soft.utils;

import java.util.Objects;
import java.util.UUID;

import drofff.crypto.algorithm.RSA;
import drofff.crypto.dto.RSAKeys;

public class KeyGenerationUtils {

	private static final int SESSION_KEY_SIZE = 16;

	private static RSAKeys rsaKeys;

	private KeyGenerationUtils() {}

	public static String generateSessionKey() {
		return UUID.randomUUID().toString()
				.substring(0, SESSION_KEY_SIZE);
	}

	public static RSAKeys getRSAKeys() {
		if(Objects.isNull(rsaKeys)) {
			rsaKeys = RSA.generateKeys();
		}
		return rsaKeys;
	}

}
