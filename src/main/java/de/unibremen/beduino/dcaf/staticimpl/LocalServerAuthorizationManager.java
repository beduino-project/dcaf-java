package de.unibremen.beduino.dcaf.staticimpl;

import de.unibremen.beduino.dcaf.*;
import de.unibremen.beduino.dcaf.datastructures.*;
import de.unibremen.beduino.dcaf.exceptions.AmbiguousPskException;
import de.unibremen.beduino.dcaf.exceptions.MacFailedException;
import de.unibremen.beduino.dcaf.exceptions.NotAuthorizedException;
import de.unibremen.beduino.dcaf.exceptions.PskNotFoundException;
import org.apache.commons.codec.binary.Hex;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

/**
 * @author Connor Lanigan
 * @author Sven Höper
 */
public class LocalServerAuthorizationManager implements ServerAuthorizationManager {

    private static Logger logger = LoggerFactory.getLogger(LocalServerAuthorizationManager.class);

    @NotNull
    private final PermissionFilter permissionFilter;
    @NotNull
    private final PskRepository pskRepository;

    private final SecureRandom r = new SecureRandom();


	private int lifetime = 3600;
	private MacMethod hmacMethod = MacMethod.HMAC_SHA_256;
	private int nonceLength = 16;

	public LocalServerAuthorizationManager(@NotNull PermissionFilter permissionFilter, @NotNull PskRepository pskRepository) {
        this.permissionFilter = permissionFilter;
        this.pskRepository = pskRepository;
    }

    @Override@NotNull
    public TicketGrantMessage process(@NotNull String camIdentifier, @NotNull TicketRequestMessage ticketRequestMessage)
            throws NotAuthorizedException, AmbiguousPskException, PskNotFoundException, MacFailedException {

        // authorize
        TicketRequestMessage tRM = permissionFilter.filter(camIdentifier, ticketRequestMessage);

        // generate Face
        Face face = generateFace(tRM);

        try {
	        // generate Verifier
	        Verifier verifier = generateVerifier(face);
	        return new TicketGrantMessage(face, verifier);

        } catch (Exception e) {
            throw new MacFailedException(e);
        }

    }

    /**
     * @param authorizations AIF to get PSK for
     * @return PSK for requested URI
     * @throws AmbiguousPskException when the requested URIs need different PSKs
     */
    @NotNull
    private Optional<byte[]> getPsk(@NotNull Authorization[] authorizations)
            throws AmbiguousPskException, PskNotFoundException {

	    byte[] psk = null;
        for (Authorization auth : authorizations) {
            Optional<byte[]> newPsk = pskRepository.lookup(auth.getHost());

            if(newPsk.isPresent()) {
            	byte[] newPskBytes = newPsk.get();
	            if (psk != null && !Arrays.equals(newPskBytes, psk)) {
		            throw new AmbiguousPskException();
	            } else {
	            	psk = newPskBytes;
	            }

            } else {
                throw new PskNotFoundException(auth.getHost());
            }
        }

        return Optional.ofNullable(psk);
    }

    /**
     * @param ticketRequestMessage ticket request message to generate Face for
     * @return ticket grant message including the generated face
     */
    @NotNull
    private Face generateFace(@NotNull TicketRequestMessage ticketRequestMessage) {
        Date timestamp = new Date();
        byte[] nonce = new byte[nonceLength];
        r.nextBytes(nonce);

        return new Face(ticketRequestMessage.sai, timestamp, lifetime, nonce, hmacMethod);
    }

    /**
     * @param face the the verifier should be generated for
     * @return Verifier
     */
    @NotNull
    private Verifier generateVerifier(@NotNull Face face) throws MacFailedException, InvalidKeyException {
        Optional<byte[]> cborData = Utils.serializeCbor(face);

        if(cborData.isPresent()) {
	        Optional<byte[]> psk = getPsk(face.getSai());

	        if (psk.isPresent()) {
		        logger.debug("computeMac with payload: " + Hex.encodeHexString(cborData.get())
				        + " and algorithm: " + face.getMacMethod().algorithmName);
		        byte[] mac = Utils.computeMac(face.getMacMethod(), psk.get(), cborData.get());

		        return new Verifier(mac);
	        }
        }

		return new Verifier(new byte[]{});
    }

	public int getLifetime() {
		return lifetime;
	}

	public void setLifetime(int lifetime) {
		this.lifetime = lifetime;
	}

	public MacMethod getHmacMethod() {
		return hmacMethod;
	}

	public void setHmacMethod(MacMethod hmacMethod) {
		this.hmacMethod = hmacMethod;
	}

	public int getNonceLength() {
		return nonceLength;
	}

	public void setNonceLength(int nonceLength) {
		this.nonceLength = nonceLength;
	}

}
