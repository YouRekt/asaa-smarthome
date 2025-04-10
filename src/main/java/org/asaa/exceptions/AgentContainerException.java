package org.asaa.exceptions;

import static java.lang.String.format;

public class AgentContainerException extends RuntimeException {

	public AgentContainerException(final String msg,final Throwable cause) {
		super(format("Couldn't run agent controller for agent: %s.", msg), cause);
	}
}
