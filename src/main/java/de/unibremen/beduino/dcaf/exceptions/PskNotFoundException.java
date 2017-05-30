package de.unibremen.beduino.dcaf.exceptions;

/**
 * @author Connor Lanigan
 * @author Sven Höper
 */
public class PskNotFoundException extends RuntimeException {

	private final String hostname;

	public PskNotFoundException(String hostname){
		this.hostname = hostname;
	}
	public PskNotFoundException(String hostname, Throwable cause){
		super(cause);
		this.hostname = hostname;
	}

	@Override
	public String toString(){
		return "No PSK could be found for the host \""+hostname+"\".";
	}
}
