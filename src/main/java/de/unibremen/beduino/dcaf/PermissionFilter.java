package de.unibremen.beduino.dcaf;

import de.unibremen.beduino.dcaf.datastructures.Authorization;
import de.unibremen.beduino.dcaf.datastructures.TicketRequestMessage;
import de.unibremen.beduino.dcaf.exceptions.NotAuthorizedException;
import org.jetbrains.annotations.NotNull;

import java.net.URI;

/**
 * @author Connor Lanigan
 * @author Sven Höper
 */
public abstract class PermissionFilter {

    /**
     * @param camIdentifier Client Authorization Manager Identifier
     * @param ticketRequestMessage Ticket Request Message
     * @return TicketRequestMessage mit den Berechtigungen die gewährt wurden
     */
    public TicketRequestMessage filter(@NotNull String camIdentifier, @NotNull TicketRequestMessage ticketRequestMessage)
                                    throws NotAuthorizedException {
        TicketRequestMessage tRM = doFilter(camIdentifier, ticketRequestMessage);

        for (Authorization a : tRM.sai) {
            URI uri = URI.create(a.getUri());
            a.setHost(uri.getHost());
            a.setUri(uri.getPath());
        }

        return tRM;
    }

    protected abstract TicketRequestMessage doFilter(@NotNull String camIdentifier, @NotNull TicketRequestMessage ticketRequestMessage)
            throws NotAuthorizedException;

}
