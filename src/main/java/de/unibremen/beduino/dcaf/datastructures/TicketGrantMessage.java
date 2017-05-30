package de.unibremen.beduino.dcaf.datastructures;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * von SAM zu CAM zu C
 *
 * @author Connor Lanigan
 * @author Sven Höper
 */
@JsonSerialize(using = TicketGrantMessage.TicketGrantMessageSerializer.class)
public class TicketGrantMessage {
	private final Face face; // F
	private final Verifier verifier; // V

	public Face getFace() {
		return face;
	}

	public Verifier getVerifier() {
		return verifier;
	}

	public TicketGrantMessage(@NotNull Face face, @NotNull Verifier verifier) {
		this.face = face;
		this.verifier = verifier;
	}

	public TicketGrantMessage(@NotNull String encryptedMessage, @NotNull String key, @NotNull Verifier verifier) {
		this.face = null;
		this.verifier = verifier;
	}

	protected static class TicketGrantMessageSerializer extends JsonSerializer<TicketGrantMessage> {

		@Override
		public void serialize(TicketGrantMessage value, JsonGenerator jgen, SerializerProvider provider)
				throws IOException {
			jgen.writeStartObject();
			jgen.writeFieldId(8);
			jgen.writeObject(value.getFace());
			jgen.writeFieldId(9);
			jgen.writeObject(value.getVerifier().getVerifier());
			jgen.writeEndObject();
		}
	}
}
