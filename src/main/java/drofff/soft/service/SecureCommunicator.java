package drofff.soft.service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import drofff.crypto.algorithm.RSA;
import drofff.crypto.dto.RSAKey;
import drofff.crypto.dto.RSAKeys;
import drofff.crypto.mode.CipherMode;
import drofff.crypto.utils.ArrayUtils;
import drofff.soft.enums.CommunicationMode;
import drofff.soft.exception.LineException;
import drofff.soft.utils.DataStreamUtils;
import drofff.soft.utils.KeyGenerationUtils;

public class SecureCommunicator extends Communicator {

	private static final String PUBLIC_KEY_PREFIX = "(";
	private static final String PUBLIC_KEY_SUFFIX = ")";
	private static final String PUBLIC_KEY_SEPARATOR = ",";

	private static final String SERVER_ACKNOWLEDGMENT = "ACK";
	private static final int ACKNOWLEDGEMENT_WAIT_TIME = 3;

	private Socket socket;
	private CommunicationMode communicationMode;

	private CipherMode encoder;
	private CipherMode decoder;

	private String sessionKey;

	SecureCommunicator(Socket socket, Scanner scanner,
	                   CipherMode encoder, CipherMode decoder,
	                   CommunicationMode communicationMode) {
		super(socket, scanner);
		this.socket = socket;
		this.communicationMode = communicationMode;
		this.encoder = encoder;
		this.decoder = decoder;
	}

	@Override
	public void run() {
		try {
			System.out.println("Establishing connection with " + socket.getInetAddress().toString() + "...");
			establishSecureConnection();
			super.run();
		} catch(IOException e) {
			throw new LineException("Error while establishing connection: " + e.getMessage());
		}
	}

	private void establishSecureConnection() throws IOException {
		if(communicationMode.equals(CommunicationMode.SERVER)) {
			establishSecureServerConnection();
		} else if(communicationMode.equals(CommunicationMode.CLIENT)) {
			establishSecureClientConnection();
		}
	}

	private void establishSecureServerConnection() throws IOException {
		RSAKeys rsaKeys = KeyGenerationUtils.getRSAKeys();
		DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
		DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
		String publicKeyStr = publicKeyToStr(rsaKeys.getPublicKey());
		dataOutputStream.writeUTF(publicKeyStr);
		DataStreamUtils.waitTillHasAvailableBytes(dataInputStream);
		int[] encryptedSessionKey = DataStreamUtils.readIntArrayFromDataStream(dataInputStream);
		sessionKey = decryptSessionKey(encryptedSessionKey, rsaKeys.getPrivateKey());
		dataOutputStream.writeUTF(SERVER_ACKNOWLEDGMENT);
	}

	private String publicKeyToStr(RSAKey rsaKey) {
		String exponent = rsaKey.getExponent().toString();
		String module = rsaKey.getModule().toString();
		return PUBLIC_KEY_PREFIX + exponent + PUBLIC_KEY_SEPARATOR + module + PUBLIC_KEY_SUFFIX;
	}

	private String decryptSessionKey(int[] encryptedSessionKey, RSAKey privateKey) {
		int[] sessionKeyArray = RSA.applyKey(encryptedSessionKey, privateKey);
		return ArrayUtils.intArrayToStr(sessionKeyArray);
	}

	private void establishSecureClientConnection() throws IOException {
		sessionKey = KeyGenerationUtils.generateSessionKey();
		DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
		DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
		DataStreamUtils.waitTillHasAvailableBytes(dataInputStream);
		String serverPublicKey = dataInputStream.readUTF();
		RSAKey publicKey = parseServerPublicKey(serverPublicKey);
		int[] encryptedSessionKey = encryptSessionKeyWithPublicKey(sessionKey, publicKey);
		DataStreamUtils.writeIntArrayToDataStream(encryptedSessionKey, dataOutputStream);
		waitForServerAcknowledgment(dataInputStream);
	}

	private RSAKey parseServerPublicKey(String serverPublicKey) {
		String publicKey = serverPublicKey.replace(PUBLIC_KEY_PREFIX, "")
				.replace(PUBLIC_KEY_SUFFIX, "");
		String[] keyParts = publicKey.split(PUBLIC_KEY_SEPARATOR);
		long exponent = Long.parseLong(keyParts[0]);
		long module = Long.parseLong(keyParts[1]);
		return buildRSAKey(exponent, module);
	}

	private RSAKey buildRSAKey(long exponent, long module) {
		RSAKey rsaKey = new RSAKey();
		rsaKey.setExponent(exponent);
		rsaKey.setModule(module);
		return rsaKey;
	}

	private int[] encryptSessionKeyWithPublicKey(String sessionKey, RSAKey publicKey) {
		int[] sessionKeyArray = ArrayUtils.strToIntArray(sessionKey);
		return RSA.applyKey(sessionKeyArray, publicKey);
	}

	private void waitForServerAcknowledgment(DataInputStream inputStream) throws IOException {
		for(int i = 0; i < ACKNOWLEDGEMENT_WAIT_TIME; i++) {
			String message = inputStream.readUTF();
			if(message.equals(SERVER_ACKNOWLEDGMENT)) {
				break;
			}
		}
	}

	@Override
	protected String encryptMessage(String message) {
		return encoder.apply(message, sessionKey);
	}

	@Override
	protected String decryptMessage(String encryptedMessage) {
		return decoder.apply(encryptedMessage, sessionKey);
	}

}
