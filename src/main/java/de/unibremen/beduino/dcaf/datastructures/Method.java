package de.unibremen.beduino.dcaf.datastructures;

/**
 * @author Connor Lanigan
 * @author Sven Höper
 */
public enum Method {
	GET(1),
	POST(2),
	PUT(4),
	DELETE(8),
	PATCH(16);

	public final int bit;

	Method(int bit) {
		this.bit = bit;
	}
}