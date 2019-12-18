package drofff.soft.events;

import drofff.soft.enums.State;

public class ClientCommunicationEvent extends CommunicationEvent {

	private static final long EVENT_CODE = 1;

	public ClientCommunicationEvent(State state) {
		super(state);
	}

	@Override
	public Long getCode() {
		return EVENT_CODE;
	}

}
