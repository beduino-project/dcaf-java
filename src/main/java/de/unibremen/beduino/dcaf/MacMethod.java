package de.unibremen.beduino.dcaf;

/**
 * @author Connor Lanigan
 * @author Sven Höper
 */
public enum MacMethod {
    HMAC_SHA_256(0, "HmacSHA256"),
    HMAC_SHA_384(1, "HmacSHA384"),
    HMAC_SHA_512(2, "HmacSHA512");

    public final int encoding;
    public final String algorithmName;

    MacMethod(int encoding, String algorithmName) {
        this.encoding = encoding;
        this.algorithmName = algorithmName;
    }
}
