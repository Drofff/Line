package drofff.soft.service;

import com.drofff.crypto.algorithm.AES;
import com.drofff.crypto.algorithm.CryptoAlgorithm;
import com.drofff.crypto.enums.Size;
import com.drofff.crypto.mode.CBCDecoder;
import com.drofff.crypto.mode.CBCEncoder;
import com.drofff.crypto.mode.CipherMode;
import drofff.soft.constants.AddressConstants;
import drofff.soft.enums.CommunicationMode;
import drofff.soft.enums.State;
import drofff.soft.events.ClientCommunicationEvent;
import drofff.soft.events.Event;
import drofff.soft.events.EventsBroker;
import drofff.soft.events.ServerCommunicationEvent;
import drofff.soft.utils.CommunicationUtils;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Optional;

public class LineClient extends Service {

	private final int serverPort;

	public LineClient(EventsBroker eventsBroker, int serverPort) {
		super(eventsBroker);
		this.serverPort = serverPort;
		registerEventProcessors();
	}

	private void registerEventProcessors() {
		ServerCommunicationEvent serverCommunicationEvent = new ServerCommunicationEvent(State.START);
		registerEventProcessor(serverCommunicationEvent, this::processServerCommunicationEvent);
	}

	private void processServerCommunicationEvent(Event event) {
		ServerCommunicationEvent serverCommunicationEvent = (ServerCommunicationEvent) event;
		if(serverCommunicationEvent.getState() == State.START) {
			setActive(false);
		} else if(serverCommunicationEvent.getState() == State.FINISH) {
			setActive(true);
		}
	}

	@Override
	void serve() {
		String destinationAddress = CommunicationUtils.askForIpAddress("Enter destination IP address to start chat: ");
		if(isNotNullAddress(destinationAddress)) {
			Optional<Socket> socket = connectToServer(destinationAddress);
			socket.ifPresent(this::runChat);
		}
	}

	private boolean isNotNullAddress(String address) {
		return !address.equals(AddressConstants.NULL_ADDRESS);
	}

	private Optional<Socket> connectToServer(String address) {
		Socket socket;
		try {
			socket = new Socket(address, serverPort);
		} catch(UnknownHostException e) {
			System.out.println("Destination with such address isn't reachable");
			return Optional.empty();
		} catch(IOException e) {
			System.out.println("Error while connecting to destination: " + e.getMessage());
			return Optional.empty();
		}
		return Optional.of(socket);
	}

	private void runChat(Socket socket) {
		sendCommunicationStartEventToServer();
		CryptoAlgorithm aes = new AES(Size._128_BITS, Size._128_BITS);
		CipherMode encoder = CBCEncoder.withCryptoAlgorithm(aes);
		CipherMode decoder = CBCDecoder.withCryptoAlgorithm(aes);
		Communicator communicator = new SecureCommunicator(socket, encoder, decoder, CommunicationMode.CLIENT);
		communicator.run();
		sendCommunicationFinishEventToServer();
	}

	private void sendCommunicationStartEventToServer() {
		sendCommunicationEventToServer(State.START);
	}

	private void sendCommunicationFinishEventToServer() {
		sendCommunicationEventToServer(State.FINISH);
	}

	private void sendCommunicationEventToServer(State state) {
		sendEventToServer(new ClientCommunicationEvent(state));
	}

}
