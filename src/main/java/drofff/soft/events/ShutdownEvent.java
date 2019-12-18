package drofff.soft.events;

public class ShutdownEvent extends Event {

	private static final long EVENT_CODE = 0;

	@Override
	public Long getCode() {
		return EVENT_CODE;
	}

}
