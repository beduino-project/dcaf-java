package de.unibremen.beduino.dcaf.exceptions;

/**
 * @author Connor Lanigan
 * @author Sven Höper
 */
public class MacFailedException extends RuntimeException {
    public MacFailedException(Exception e) {
        super(e);
    }

    public MacFailedException() {}
}
