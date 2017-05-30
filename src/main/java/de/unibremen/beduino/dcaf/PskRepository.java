package de.unibremen.beduino.dcaf;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * @author Connor Lanigan
 * @author Sven Höper
 */
public interface PskRepository {
	@NotNull
	Optional<byte[]> lookup(String hostname);
}
