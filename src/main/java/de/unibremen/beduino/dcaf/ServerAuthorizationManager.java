package de.unibremen.beduino.dcaf;

import de.unibremen.beduino.dcaf.datastructures.TicketGrantMessage;
import de.unibremen.beduino.dcaf.datastructures.TicketRequestMessage;
import de.unibremen.beduino.dcaf.exceptions.AmbiguousPskException;
import de.unibremen.beduino.dcaf.exceptions.MacFailedException;
import de.unibremen.beduino.dcaf.exceptions.NotAuthorizedException;
import de.unibremen.beduino.dcaf.exceptions.PskNotFoundException;
import org.jetbrains.annotations.NotNull;

/**
 * @author Connor Lanigan
 * @author Sven Höper
 */
public interface ServerAuthorizationManager {
	@NotNull TicketGrantMessage process(@NotNull String camIdentifier, @NotNull TicketRequestMessage ticketRequestMessage)
	        throws NotAuthorizedException, AmbiguousPskException, PskNotFoundException, MacFailedException;
}
