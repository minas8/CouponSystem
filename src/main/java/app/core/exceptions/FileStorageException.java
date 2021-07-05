package app.core.exceptions;

public class FileStorageException extends CouponSystemException {

	private static final long serialVersionUID = 1L;

	public FileStorageException() {
	}

	public FileStorageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public FileStorageException(String message, Throwable cause) {
		super(message, cause);
	}

	public FileStorageException(String message) {
		super(message);
	}

	public FileStorageException(Throwable cause) {
		super(cause);
	}

}
