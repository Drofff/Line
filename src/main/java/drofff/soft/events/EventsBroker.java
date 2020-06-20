package drofff.soft.events;

import java.util.ArrayList;
import java.util.List;

public class EventsBroker {

	private final List<Event> serverEvents = new ArrayList<>();

	private final List<Event> clientEvents = new ArrayList<>();

	public void sendEventToClient(Event event) {
		putEventIntoList(event, clientEvents);
	}

	public void sendEventToServer(Event event) {
		putEventIntoList(event, serverEvents);
	}

	private void putEventIntoList(Event event, List<Event> list) {
		if(isEventInList(event, list)) {
			replacePreviousEventInList(event, list);
		} else {
			list.add(event);
		}
	}

	private boolean isEventInList(Event event, List<Event> list) {
		return list.contains(event);
	}

	private void replacePreviousEventInList(Event event, List<Event> list) {
		list.remove(event);
		list.add(event);
	}

	public List<Event> getServerEvents() {
		return new ArrayList<>(serverEvents);
	}

	public List<Event> getClientEvents() {
		return new ArrayList<>(clientEvents);
	}

	public void clearServerEvents() {
		serverEvents.clear();
	}

	public void clearClientEvents() {
		clientEvents.clear();
	}

}
