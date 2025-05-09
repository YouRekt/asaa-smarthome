package org.asaa.exceptions;

public class InvalidServiceSpecification extends RuntimeException {

    public InvalidServiceSpecification(final Throwable cause) {
        super("Couldn't create agent's service.", cause);
    }
}
