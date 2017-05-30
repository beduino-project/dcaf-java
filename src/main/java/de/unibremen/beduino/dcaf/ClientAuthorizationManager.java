package de.unibremen.beduino.dcaf;

import de.unibremen.beduino.dcaf.datastructures.AccessRequest;
import de.unibremen.beduino.dcaf.datastructures.TicketGrantMessage;
import de.unibremen.beduino.dcaf.datastructures.TicketRequestMessage;
import de.unibremen.beduino.dcaf.exceptions.CredentialsInvalidException;
import de.unibremen.beduino.dcaf.utils.Tuple;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * @author Connor Lanigan
 * @author Sven Höper
 */
public class ClientAuthorizationManager {

	private static Logger logger = LoggerFactory.getLogger(ClientAuthorizationManager.class);

	private String camIdentifier;

	public void setIdentifier(String camIdentifier) {
		this.camIdentifier = camIdentifier;
	}

	/**
	 * Authentisiert einen Client und stellt diesem ein Ticket aus.
	 *
	 * @param message
	 * @return
	 */
	@NotNull
	public TicketGrantMessage process(@NotNull AccessRequest message)
			throws CredentialsInvalidException {

		// leite Anfrage an ServerAuthorizationManager weiter & empfange Antwort
		TicketRequestMessage trm = new TicketRequestMessage(message.samUrl, message.sai, message.timestamp);

		ServerAuthorizationManager sam = lookup(message.samUrl);

		// leite Antwort an Client weiter
		return sam.process(camIdentifier, trm);
	}


	private Set<SamLocator> locators = new HashSet<>();

	public void registerSamLocator(SamLocator locator) {
		locators.add(locator);
	}

	public void unregisterSamLocator(SamLocator locator) {
		locators.remove(locator);
	}

	private class ResultComparator implements Comparator<Tuple<Integer, ServerAuthorizationManager>> {

		@Override
		public int compare(Tuple<Integer, ServerAuthorizationManager> o1, Tuple<Integer, ServerAuthorizationManager> o2) {
			return o1._1.compareTo(o2._1);
		}

	}

	protected ServerAuthorizationManager lookup(String samUrl) {
		SortedSet<Tuple<Integer, ServerAuthorizationManager>> results = new TreeSet<>(new ResultComparator());


		try {
			URI uri = new URI(samUrl);

			for (SamLocator loc : locators) {
				Optional<Tuple<Integer, ServerAuthorizationManager>> res = loc.findSam(uri);
				res.ifPresent(results::add);
			}

			if (!results.isEmpty())
				return results.first()._2;
		} catch (URISyntaxException e) {
			logger.warn("findSam failed", e);
		}

		throw new UnsupportedOperationException("No known SAM connector was found for the SAM URL " + samUrl);

	}
}
