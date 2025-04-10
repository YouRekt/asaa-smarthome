package org.asaa.exceptions;

public class JadePlatformInitializationException extends RuntimeException {

	public JadePlatformInitializationException(final Throwable cause) {
		super("Couldn't initialize JADE platform.", cause);
	}
}
