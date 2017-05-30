package de.unibremen.beduino.dcaf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import de.unibremen.beduino.dcaf.datastructures.Face;
import de.unibremen.beduino.dcaf.exceptions.MacFailedException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.Optional;

/**
 * @author Connor Lanigan
 * @author Sven Höper
 */
public class Utils {

    private static Logger logger = LoggerFactory.getLogger(Utils.class);
    private static final CBORFactory F = new CBORFactory();
    private static final ObjectMapper MAPPER = new ObjectMapper(F);

    // TODO: change MediaType to application/cbor+dcaf once specified
    /**
     * DCAF MediaType to answer with
     * @see <a href="https://www.iana.org/assignments/core-parameters/core-parameters.xhtml#table-content-formats">Constrained RESTful Environments (CoRE) Parameters: CoAP Content-Formats</a>
     */
    private static final int DCAF_MEDIA_TYPE = 60; // application/cbor

    private Utils() {}

    /**
     * Compute MAC for a given byte array
     *
     * @param macMethod key generation method to use
     * @param key key to use when applying the KDF
     * @param input input we want to compute a MAC for
     * @return computed MAC
     */
    @NotNull
    public static byte[] computeMac(@NotNull final MacMethod macMethod,
                                    @NotNull final byte[] key,
                                    @NotNull final byte[] input)
            throws MacFailedException, InvalidKeyException {

        try {
	        Mac mac = Mac.getInstance(macMethod.algorithmName);
	        SecretKeySpec secretKey = new SecretKeySpec(key, macMethod.algorithmName);
	        mac.init(secretKey);

	        return mac.doFinal(input);
        } catch (InvalidKeyException e) {
	        throw e;
        } catch (Exception e) {
	        throw new MacFailedException(e);
        }
    }

    /**
     * Compute MAC for a given {@see Face}
     *
     * @param macMethod {@see MacMethod} to use
     * @param key Key to use
     * @param face Face to serialze and compute the MAC for
     * @return MAC
     * @throws MacFailedException in case there was an error
     * @throws InvalidKeyException if the given key was inappropriate to initialize the MAC
     */
    @NotNull
    public static byte[] computeMac(@NotNull final MacMethod macMethod,
                                    @NotNull final byte[] key,
                                    @NotNull final Face face)
            throws MacFailedException, InvalidKeyException {

        Optional<byte[]> input = serializeCbor(face);

        if(input.isPresent()) {
            return computeMac(macMethod, key, input.get());
        }
        throw new MacFailedException();
    }

    /**
     * Serialize given object as CBOR
     *
     * @param object to serialize
     * @return Optional of serialized input or Optional.empty()
     */
    @NotNull
    public static <T> Optional<byte[]> serializeCbor(T object) {
        try {
            return Optional.of(MAPPER.writeValueAsBytes(object));
        } catch (IOException e) {
            logger.error("Error 500", e);
            return Optional.empty();
        }
    }

    /**
     * Deserialize given object from CBOR
     *
     * @param requestPayload to deserialize
     * @param valueType Type to deserialize as
     * @return Optional of deserialized input or Optional.empty()
     */
    @NotNull
    public static <T> Optional<T> deserializeCbor(byte[] requestPayload, Class<T> valueType) {
        try{
            return Optional.of(MAPPER.readValue(requestPayload, valueType));
        } catch (IOException e) {
            logger.error("Error 500", e);
            return Optional.empty();
        }
    }

    /**
     * DCAF MediaType to answer with
     *
     * @return DCAF MediaType ID
     * @see <a href="https://www.iana.org/assignments/core-parameters/core-parameters.xhtml#table-content-formats">Constrained RESTful Environments (CoRE) Parameters: CoAP Content-Formats</a>
     */
    public static int getDcafMediaType() {
        return DCAF_MEDIA_TYPE;
    }
}
