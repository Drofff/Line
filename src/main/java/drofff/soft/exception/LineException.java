package drofff.soft.exception;

public class LineException extends RuntimeException {

	public LineException() {
		super();
	}

	public LineException(String message) {
		super(message);
	}

	public LineException(String message, Throwable cause) {
		super(message, cause);
	}

	public LineException(Throwable cause) {
		super(cause);
	}

	protected LineException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
