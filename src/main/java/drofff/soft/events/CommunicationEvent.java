package drofff.soft.events;

import drofff.soft.enums.State;

public abstract class CommunicationEvent extends Event {

	private final State state;

	public CommunicationEvent(State state) {
		this.state = state;
	}

	public State getState() {
		return state;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

}
