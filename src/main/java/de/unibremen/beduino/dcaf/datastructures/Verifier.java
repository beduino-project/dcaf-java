package de.unibremen.beduino.dcaf.datastructures;

/**
 * @author Connor Lanigan
 * @author Sven Höper
 */
public class Verifier {
	private final byte[] verifier;

	public Verifier(byte[] verifier) {
		this.verifier = verifier;
	}

	public byte[] getVerifier() {
		return verifier;
	}
}
