package drofff.soft.service;

import com.drofff.crypto.algorithm.AES;
import com.drofff.crypto.algorithm.CryptoAlgorithm;
import com.drofff.crypto.enums.Size;
import com.drofff.crypto.mode.CBCDecoder;
import com.drofff.crypto.mode.CBCEncoder;
import com.drofff.crypto.mode.CipherMode;
import drofff.soft.enums.CommunicationMode;
import drofff.soft.enums.State;
import drofff.soft.events.*;
import drofff.soft.utils.CommunicationUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class LineServer extends Service {

	private static final String YES_ANSWER = "yes";
	private static final String NO_ANSWER = "no";

	private final ServerSocket serverSocket;

	public LineServer(int port, EventsBroker eventsBroker) throws IOException {
		super(eventsBroker);
		serverSocket = new ServerSocket(port);
		registerEventProcessors();
	}

	private void registerEventProcessors() {
		ShutdownEvent shutdownEvent = new ShutdownEvent();
		registerEventProcessor(shutdownEvent, event -> closeServer());
		ClientCommunicationEvent clientCommunicationEvent = new ClientCommunicationEvent(State.START);
		registerEventProcessor(clientCommunicationEvent, this::processClientCommunicationEvent);
	}

	private void closeServer() {
		try {
			serverSocket.close();
		} catch(IOException e) {
			System.out.println("Error while closing socket server: " + e.getMessage());
		}
	}

	private void processClientCommunicationEvent(Event event) {
		ClientCommunicationEvent clientCommunicationEvent = (ClientCommunicationEvent) event;
		if(clientCommunicationEvent.getState().equals(State.START)) {
			setActive(false);
		} else if(clientCommunicationEvent.getState().equals(State.FINISH)) {
			setActive(true);
		}
	}

	@Override
	void serve() {
		try {
			processRequest();
		} catch(IOException e) {
			System.out.println("Error while processing incoming request: " + e.getMessage());
		}
	}

	private void processRequest() throws IOException {
		Socket socket = serverSocket.accept();
		sendCommunicationStartEventToClient();
		String ipAddress = socket.getInetAddress().toString();
		String question = "There is a connection attempt from ip address " + ipAddress + "\nAccept?";
		String decision = CommunicationUtils.askQuestion(question, YES_ANSWER, NO_ANSWER);
		if(decision.equals(YES_ANSWER)) {
			CommunicationUtils.clearAnswerStack();
			runChat(socket);
		} else if(decision.equals(NO_ANSWER)) {
			System.out.println("Okay. Request has been successfully refused");
		}
		sendCommunicationFinishEventToClient();
	}

	private void runChat(Socket socket) {
		CryptoAlgorithm aes = new AES(Size._128_BITS, Size._128_BITS);
		CipherMode encoder = CBCEncoder.withCryptoAlgorithm(aes);
		CipherMode decoder = CBCDecoder.withCryptoAlgorithm(aes);
		Communicator communicator = new SecureCommunicator(socket, encoder, decoder, CommunicationMode.SERVER);
		communicator.run();
	}

	private void sendCommunicationStartEventToClient() {
		sendCommunicationEventToClient(State.START);
	}

	private void sendCommunicationFinishEventToClient() {
		sendCommunicationEventToClient(State.FINISH);
	}

	private void sendCommunicationEventToClient(State state) {
		sendEventToClient(new ServerCommunicationEvent(state));
	}

}
