package app.core.exceptions;

public class JwtExpiredException extends CouponSystemException {

	private static final long serialVersionUID = 1L;

	public JwtExpiredException() {
	}

	public JwtExpiredException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public JwtExpiredException(String message, Throwable cause) {
		super(message, cause);
	}

	public JwtExpiredException(String message) {
		super(message);
	}

	public JwtExpiredException(Throwable cause) {
		super(cause);
	}

}
