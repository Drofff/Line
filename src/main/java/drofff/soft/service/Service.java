package drofff.soft.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import drofff.soft.events.Event;
import drofff.soft.events.EventsBroker;
import drofff.soft.events.ShutdownEvent;
import drofff.soft.utils.CommunicationUtils;

public abstract class Service {

	private static final long SLEEP_MILLIS = 1000;

	private final EventsBroker eventsBroker;
	private final Map<Long, Consumer<Event>> eventsProcessorsRegistry = new HashMap<>();

	private boolean stop = false;
	private boolean active = true;

	protected Service(EventsBroker eventsBroker) {
		this.eventsBroker = eventsBroker;
	}

	public void run() {
		while(!stop) {
			processEvents();
			if(active) {
				serve();
			} else {
				CommunicationUtils.sleep(SLEEP_MILLIS);
			}
		}
	}

	private void processEvents() {
		List<Event> events = eventsBroker.getClientEvents();
		events.forEach(this::processEvent);
		events.clear();
	}

	private void processEvent(Event event) {
		if(event instanceof ShutdownEvent) {
			stop = true;
		} else {
			applyEventProcessorIfRegistered(event);
		}
	}

	private void applyEventProcessorIfRegistered(Event event) {
		Long eventCode = event.getCode();
		if(eventsProcessorsRegistry.containsKey(eventCode)) {
			Consumer<Event> eventProcessor = eventsProcessorsRegistry.get(eventCode);
			eventProcessor.accept(event);
		}
	}

	protected void registerEventProcessor(Event event, Consumer<Event> eventProcessor) {
		Long eventCode = event.getCode();
		eventsProcessorsRegistry.put(eventCode, eventProcessor);
	}

	protected boolean isActive() {
		return active;
	}

	protected void setActive(boolean active) {
		this.active = active;
	}

	protected void sendEventToServer(Event event) {
		eventsBroker.sendEventToServer(event);
	}

	protected void sendEventToClient(Event event) {
		eventsBroker.sendEventToClient(event);
	}

	abstract void serve();

}
