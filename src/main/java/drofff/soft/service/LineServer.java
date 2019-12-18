package drofff.soft.service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import drofff.soft.enums.State;
import drofff.soft.events.ClientCommunicationEvent;
import drofff.soft.events.Event;
import drofff.soft.events.EventsBroker;
import drofff.soft.events.ServerCommunicationEvent;
import drofff.soft.events.ShutdownEvent;
import drofff.soft.utils.CommunicationUtils;

public class LineServer extends Service {

	private static final String YES_ANSWER = "yes";
	private static final String NO_ANSWER = "no";

	private ServerSocket serverSocket;
	private Scanner scanner;

	public LineServer(int port, Scanner scanner, EventsBroker eventsBroker) throws IOException {
		super(eventsBroker);
		serverSocket = new ServerSocket(port);
		this.scanner = scanner;
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
			runChat(socket);
		} else if(decision.equals(NO_ANSWER)) {
			System.out.println("Okay. Request has been successfully refused");
		}
		sendCommunicationFinishEventToClient();
	}

	private void runChat(Socket socket) {
		Communicator communicator = new Communicator(socket, scanner);
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
