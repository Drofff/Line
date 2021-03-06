package drofff.soft.service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static drofff.soft.utils.CommunicationUtils.getUserInput;

public class Communicator {

	private static final String EXIT_CODE = "::exit";
	private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

	private final Socket socket;
	private final String address;

	private volatile boolean stop = false;

	public Communicator(Socket socket) {
		this.socket = socket;
		this.address = socket.getInetAddress().toString();
	}

	public void run() {
		EXECUTOR.execute(this::displayReceivedMessages);
		sendUserMessages();
		closeConnection();
	}

	private void sendUserMessages() {
		try {
			sendMessages();
		} catch(IOException e) {
			System.out.println("Error while sending message: " + e.getMessage());
		}
	}

	private void sendMessages() throws IOException {
		System.out.println("Chat with " + address + " has started. To exit please enter the following command: " + EXIT_CODE);
		DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
		while(!stop) {
			String message = getUserInput();
			validateIsExitCode(message);
			message = encryptMessage(message);
			dataOutputStream.writeUTF(message);
		}
	}

	private void displayReceivedMessages() {
		try {
			displayInputMessages();
		} catch(IOException e) {
			System.out.println("Error while reading messages: " + e.getMessage());
		}
	}

	private void displayInputMessages() throws IOException {
		DataInputStream messagesStream = new DataInputStream(socket.getInputStream());
		while(!stop) {
			if(messagesStream.available() > 0) {
				String message = messagesStream.readUTF();
				message = decryptMessage(message);
				validateIsExitCode(message);
				System.out.println(address + ": " + message);
			}
		}
	}

	private void validateIsExitCode(String message) {
		if(message.equals(EXIT_CODE)) {
			stop = true;
		}
	}

	private void closeConnection() {
		try {
			socket.close();
		} catch(IOException e) {
			System.out.println("Error while closing connection: " + e.getMessage());
		}
	}

	protected String encryptMessage(String message) {
		return message;
	}

	protected String decryptMessage(String message) {
		return message;
	}

}
