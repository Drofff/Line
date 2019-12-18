package drofff.soft.service;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.Scanner;

import drofff.soft.enums.State;
import drofff.soft.events.ClientCommunicationEvent;
import drofff.soft.events.Event;
import drofff.soft.events.EventsBroker;
import drofff.soft.events.ServerCommunicationEvent;
import drofff.soft.utils.CommunicationUtils;

public class LineClient extends Service {

	private Scanner scanner;
	private int serverPort;

	public LineClient(Scanner scanner, EventsBroker eventsBroker, int serverPort) {
		super(eventsBroker);
		this.scanner = scanner;
		this.serverPort = serverPort;
		registerEventProcessors();
	}

	private void registerEventProcessors() {
		ServerCommunicationEvent serverCommunicationEvent = new ServerCommunicationEvent(State.START);
		registerEventProcessor(serverCommunicationEvent, this::processServerCommunicationEvent);
	}

	private void processServerCommunicationEvent(Event event) {
		ServerCommunicationEvent serverCommunicationEvent = (ServerCommunicationEvent) event;
		if(serverCommunicationEvent.getState().equals(State.START)) {
			setActive(false);
		} else if(serverCommunicationEvent.getState().equals(State.FINISH)) {
			setActive(true);
		}
	}

	@Override
	void serve() {
		String destinationAddress = CommunicationUtils.askForIpAddress("Enter destination IP address to start chat: ");
		Optional<Socket> socket = connectToServer(destinationAddress);
		socket.ifPresent(this::runChat);
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
		Communicator communicator = new Communicator(socket, scanner);
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