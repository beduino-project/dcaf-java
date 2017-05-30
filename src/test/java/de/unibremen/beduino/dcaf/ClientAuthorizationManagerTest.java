package de.unibremen.beduino.dcaf;

import de.unibremen.beduino.dcaf.utils.Tuple;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

/**
 * @author Connor Lanigan
 */
public class ClientAuthorizationManagerTest {

	@Test
	public void lookupNotFound() throws Exception {
		ClientAuthorizationManager cam = new ClientAuthorizationManager();

		String samUrl = "coaps://nonexistent.example.com";

		try {
			cam.lookup(samUrl);
			fail("Lookup returned an unexpected SAM");
		} catch (UnsupportedOperationException e) {
		}

		ServerAuthorizationManager sam = mock(ServerAuthorizationManager.class);

		SamLocator locator = url -> {
			if (url.getHost().equals("nonexistent.example.com")) {
				return Optional.of(Tuple.of(1, sam));
			} else {
				return Optional.empty();
			}
		};

		cam.registerSamLocator(locator);

		assertEquals(sam, cam.lookup(samUrl));

		cam.unregisterSamLocator(locator);

		try {
			ServerAuthorizationManager lSam = cam.lookup(samUrl);
			if (sam.equals(lSam))
				fail("Lookup returned old SAM");
			else
				fail("Lookup returned an unexpected SAM");
		} catch (UnsupportedOperationException e) {
		}

	}

	@Test
	public void lookupPriority() throws Exception {
		ClientAuthorizationManager cam = new ClientAuthorizationManager();

		String samUrl = "coaps://nonexistent.example.com";

		ServerAuthorizationManager sam1 = mock(ServerAuthorizationManager.class);
		ServerAuthorizationManager sam2 = mock(ServerAuthorizationManager.class);
		ServerAuthorizationManager sam3 = mock(ServerAuthorizationManager.class);

		SamLocator locator1 = url -> {
			if (url.getHost().equals("nonexistent.example.com")) {
				return Optional.of(Tuple.of(10, sam1));
			} else {
				return Optional.empty();
			}
		};

		cam.registerSamLocator(locator1);

		assertEquals(sam1, cam.lookup(samUrl));

		SamLocator locator2 = url -> {
			if (url.getHost().equals("nonexistent.example.com")) {
				return Optional.of(Tuple.of(20, sam2));
			} else {
				return Optional.empty();
			}
		};

		cam.registerSamLocator(locator2);

		assertEquals(sam1, cam.lookup(samUrl));

		cam.unregisterSamLocator(locator1);

		assertEquals(sam2, cam.lookup(samUrl));
		cam.registerSamLocator(locator1);

		SamLocator locator3 = url -> {
			if (url.getHost().equals("nonexistent.example.com")) {
				return Optional.of(Tuple.of(5, sam3));
			} else {
				return Optional.empty();
			}
		};

		cam.registerSamLocator(locator3);
		assertEquals(sam3, cam.lookup(samUrl));

		cam.unregisterSamLocator(locator1);

		assertEquals(sam3, cam.lookup(samUrl));
	}

}