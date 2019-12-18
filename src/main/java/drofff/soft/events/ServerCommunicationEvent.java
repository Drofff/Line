package drofff.soft.events;

import drofff.soft.enums.State;

public class ServerCommunicationEvent extends CommunicationEvent {

	private static final long EVENT_CODE = 2;

	public ServerCommunicationEvent(State state) {
		super(state);
	}

	@Override
	public Long getCode() {
		return EVENT_CODE;
	}

}
