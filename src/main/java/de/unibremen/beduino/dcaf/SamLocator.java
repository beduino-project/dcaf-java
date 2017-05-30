package de.unibremen.beduino.dcaf;

import de.unibremen.beduino.dcaf.utils.Tuple;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Optional;

/**
 * @author Connor Lanigan
 * @author Sven Höper
 */
public interface SamLocator {
	@NotNull
	Optional<Tuple<Integer, ServerAuthorizationManager>> findSam(URI samUrl);
}
