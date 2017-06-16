package io.lnk.lookup.consul;

/**
 * @author scott
 *
 */
public class ConsulException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8877025362205098933L;

	public ConsulException(String error) {
		super(error);
	}

	public ConsulException(String error, Throwable tr) {
		super(error, tr);
	}
}
