package de.unibremen.beduino.dcaf.datastructures;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.unibremen.beduino.dcaf.MacMethod;

import java.io.IOException;
import java.util.Date;

/**
 * @author Connor Lanigan
 * @author Sven Höper
 */
@JsonSerialize(using = Face.FaceSerializer.class)
public class Face {
	private final Authorization[] sai;

	private final Date ts;

	// Lifetime in Seconds
	private final int lifetime;

	private final byte[] nonce;

	private final MacMethod macMethod;

	public Face(Authorization[] sai, Date ts, int lifetime, byte[] nonce, MacMethod macMethod) {
		this.sai = sai;
		this.ts = ts;
		this.lifetime = lifetime;
		this.nonce = nonce;
		this.macMethod = macMethod;
	}

	public int getLifetime() {
		return lifetime;
	}

	public byte[] getNonce() {
		return nonce;
	}

	public MacMethod getMacMethod() {
		return macMethod;
	}

	public Authorization[] getSai() {
		return sai;
	}

	public Date getTs() {
		return ts;
	}

	protected static class FaceSerializer extends JsonSerializer<Face> {

		@Override
		public void serialize(Face value, JsonGenerator jgen, SerializerProvider provider)
				throws IOException {
			jgen.writeStartObject();
			jgen.writeFieldId(1);
			jgen.writeObject(value.sai);
			jgen.writeFieldId(5);
			jgen.writeNumber(value.getTs().getTime()/1000);
			jgen.writeFieldId(6);
			jgen.writeNumber(value.getLifetime());
			jgen.writeFieldId(12);
			jgen.writeBinary(value.getNonce());
			jgen.writeFieldId(7);
			jgen.writeNumber(value.macMethod.encoding);
			jgen.writeEndObject();
		}
	}
}
