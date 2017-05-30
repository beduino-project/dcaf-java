package de.unibremen.beduino.dcaf.sam;

import de.unibremen.beduino.dcaf.MacMethod;
import de.unibremen.beduino.dcaf.PermissionFilter;
import de.unibremen.beduino.dcaf.PskRepository;
import de.unibremen.beduino.dcaf.ServerAuthorizationManager;
import de.unibremen.beduino.dcaf.datastructures.Authorization;
import de.unibremen.beduino.dcaf.datastructures.Method;
import de.unibremen.beduino.dcaf.datastructures.TicketGrantMessage;
import de.unibremen.beduino.dcaf.datastructures.TicketRequestMessage;
import de.unibremen.beduino.dcaf.exceptions.NotAuthorizedException;
import de.unibremen.beduino.dcaf.staticimpl.LocalServerAuthorizationManager;
import org.apache.commons.codec.binary.Hex;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.security.SecureRandom;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Sven Höper
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(LocalServerAuthorizationManager.class)
@PowerMockIgnore("javax.crypto.*")
public class LocalServerAuthorizationManagerTest {

	@Test
	public void correctVerifierHmacSha256() throws Exception {
		checkVerifier(MacMethod.HMAC_SHA_256, "9bdf5cb183d5300b735a6a55911e68bf8d8122b5248981dfd7300b2afa7a9bbc");
	}

	@Test
	public void correctVerifierHmacSha384() throws Exception {
		checkVerifier(MacMethod.HMAC_SHA_384, "53cfe8ce1f304455046cbb514c2db4f262e0798bd104712681e5f7dfdc667e02fa3cc94cf09ef18d699b191e3686df26");
	}

	@Test
	public void correctVerifierHmacSha512() throws Exception {
		checkVerifier(MacMethod.HMAC_SHA_512, "a02fb791b3ea04cd3c07874629dc28b9fad3fad1a9739cfa712bda95808f06af37c310e1e9bc2966e54dbff4af043da0134c8fc8ce3c0546bfa21753fa688f16");
	}

	private void checkVerifier(MacMethod macMethod, String expectedVerifier) throws Exception {
		class TestPermissionFilter extends PermissionFilter {

			@Override
			protected TicketRequestMessage doFilter(@NotNull String camIdentifier, @NotNull TicketRequestMessage ticketRequestMessage) throws NotAuthorizedException {
				return ticketRequestMessage;
			}
		}

		Authorization[] aim = {
				new Authorization("coaps://sam.example.com/s/tempC", new HashSet<Method>() {{
					add(Method.GET);
					add(Method.PUT);
				}})
		};

		Date ts = new Date((long) 1495375420 * 1000);
		TicketRequestMessage tRM = new TicketRequestMessage("coaps://sam.example.com", aim, ts);

		PskRepository psks = mock(PskRepository.class);
		PermissionFilter filter = new TestPermissionFilter();

		ServerAuthorizationManager SAM = new LocalServerAuthorizationManager(filter, psks);
		((LocalServerAuthorizationManager)SAM).setHmacMethod(macMethod);

		PowerMockito.mockStatic(SecureRandom.class);
		SecureRandom secureRandom = mock(SecureRandom.class);
		doAnswer(invocation -> {
			byte[] target = invocation.getArgument(0);
			System.arraycopy(Hex.decodeHex("21DAAC7EFAEC4C47395958B326A50890".toCharArray()), 0,
							 target, 0, target.length);
			return null;
		}).when(secureRandom).nextBytes(any(byte[].class));
		Whitebox.setInternalState(SAM, secureRandom);

		PowerMockito.mockStatic(Date.class);
		PowerMockito.whenNew(Date.class).withNoArguments().thenReturn(ts);

		when(psks.lookup(any())).thenReturn(Optional.of("key".getBytes()));

		TicketGrantMessage tGM = SAM.process("cam1", tRM);

		assertNotNull(tGM);
		assertNotNull(tGM.getFace().getNonce());
		assertArrayEquals(Hex.decodeHex("21DAAC7EFAEC4C47395958B326A50890".toCharArray()), tGM.getFace().getNonce());
		byte[] nonce = tGM.getFace().getNonce();
		assertEquals(16, nonce.length);
		assertEquals("/s/tempC", tGM.getFace().getSai()[0].getUri());
		assertTrue(tGM.getFace().getSai()[0]
				.getMethodSet().contains(Method.GET));
		assertTrue(tGM.getFace().getSai()[0]
				.getMethodSet().contains(Method.PUT));

		// {
		//	1: [["/s/tempC", 5]],
		//	5: 1495375420, 6: 3600,
		//	12: h'21DAAC7EFAEC4C47395958B326A50890',
		//	7: 0
		// }
		// Hex: A5018182682F732F74656D704305051A59219E3C06190E100C5021DAAC7EFAEC4C47395958B326A508900700
		// echo -n "HEX" | xxd -r -p | openssl dgst -sha256 -hmac "key"
		// https://www.liavaag.org/English/SHA-Generator/HMAC/
		// HMAC_SHA_256: 9bdf5cb183d5300b735a6a55911e68bf8d8122b5248981dfd7300b2afa7a9bbc
		// HMAC_SHA_384: 53cfe8ce1f304455046cbb514c2db4f262e0798bd104712681e5f7dfdc667e02fa3cc94cf09ef18d699b191e3686df26
		// HMAC_SHA_512: a02fb791b3ea04cd3c07874629dc28b9fad3fad1a9739cfa712bda95808f06af37c310e1e9bc2966e54dbff4af043da0134c8fc8ce3c0546bfa21753fa688f16
		byte[] expecteds = Hex.decodeHex(expectedVerifier.toCharArray());
		byte[] actuals = tGM.getVerifier().getVerifier();
		assertArrayEquals(expecteds, actuals);
	}

	// TODO test no authorizations granted

	// TODO test no authorizations requested

	// TODO test no psks found

	// TODO test multiple psks found

	// TODO test one psk for multiple resources
}