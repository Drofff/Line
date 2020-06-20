package drofff.soft;

import drofff.soft.events.EventsBroker;
import drofff.soft.events.ShutdownEvent;
import drofff.soft.exception.LineException;
import drofff.soft.service.LineClient;
import drofff.soft.service.LineServer;
import drofff.soft.utils.Properties;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LineApplication {

	private static final String SERVER_PORT_PROPERTY_KEY = "server.port";

	private static final Executor SERVER_EXECUTOR = Executors.newSingleThreadExecutor();
	private static final EventsBroker EVENTS_BROKER = new EventsBroker();

	private static int serverPort;

	public static void main(String [] args) {
		registerShutdownHook();
		loadProperties();
		runLineServer();
		runLineClient();
	}

	private static void registerShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			ShutdownEvent shutdownEvent = new ShutdownEvent();
			EVENTS_BROKER.sendEventToServer(shutdownEvent);
			EVENTS_BROKER.sendEventToClient(shutdownEvent);
		}));
	}

	private static void loadProperties() {
		Properties properties = new Properties();
		String serverPortStr = properties.getPropertyByKey(SERVER_PORT_PROPERTY_KEY);
		serverPort = Integer.parseInt(serverPortStr);
	}

	private static void runLineServer() {
		try {
			LineServer lineServer = new LineServer(serverPort, EVENTS_BROKER);
			SERVER_EXECUTOR.execute(lineServer::run);
		} catch(IOException e) {
			throw new LineException("Error starting server: " + e.getMessage());
		}
	}

	private static void runLineClient() {
		LineClient lineClient = new LineClient(EVENTS_BROKER, serverPort);
		lineClient.run();
	}

}
