package drofff.soft.events;

public abstract class Event {

	public abstract Long getCode();

	@Override
	public int hashCode() {
		return getCode().intValue();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Event) {
			Event event = (Event) obj;
			return event.getCode().equals(getCode());
		}
		return super.equals(obj);
	}

}
