package de.unibremen.beduino.dcaf.staticimpl;

import de.unibremen.beduino.dcaf.PermissionFilter;
import de.unibremen.beduino.dcaf.datastructures.Authorization;
import de.unibremen.beduino.dcaf.datastructures.Method;
import de.unibremen.beduino.dcaf.datastructures.TicketRequestMessage;
import de.unibremen.beduino.dcaf.exceptions.NotAuthorizedException;
import de.unibremen.beduino.dcaf.utils.Tuple;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementierung der PermissionFilter Schnittstelle
 * Prüft gegen eine statische Liste von Berechtigungen
 *
 * @author Connor Lanigan
 * @author Sven Höper
 */
public class PermissionFilterStaticImpl extends PermissionFilter {

    private HashMap<String, Set<Tuple<String, Method>>> permissions = new HashMap<>();

    public void add(String cam, String url, Method... methods){
        permissions.putIfAbsent(cam, new HashSet<>());

        Set<Tuple<String, Method>> perms = permissions.get(cam);
        for(Method method: methods){
            perms.add(Tuple.of(url, method));
        }
    }

    protected TicketRequestMessage doFilter(@NotNull String camIdentifier,
                                            @NotNull TicketRequestMessage ticketRequestMessage)
                                            throws NotAuthorizedException {

        Set<Tuple<String, Method>> allowedOps = permissions.getOrDefault(camIdentifier, Collections.emptySet());

        HashSet<Authorization> grantedAuths = new HashSet<>();

        for (Authorization authorization : ticketRequestMessage.sai) {
            HashSet<Method> methods = new HashSet<>();

            for (Method method : authorization.getMethodSet()) {

                Tuple<String, Method> authAsTuple =
		                new Tuple<>(authorization.getUri(), method);

                if (allowedOps.contains(authAsTuple)) {
                    methods.add(method);
                }
            }

            if(methods.isEmpty())
                continue;

            grantedAuths.add(new Authorization(authorization.getUri(), methods));
        }

        return new TicketRequestMessage(ticketRequestMessage.samUrl,
                                        grantedAuths.toArray(new Authorization[0]),
                                        ticketRequestMessage.timestamp);
    }

}
