package de.unibremen.beduino.dcaf.datastructures;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Connor Lanigan
 * @author Sven Höper
 */
@JsonSerialize(using = Authorization.AuthorizationSerializer.class)
public class Authorization {
	private String uri;
	private String host;
	private final byte methods;

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public byte getMethods() {
		return methods;
	}

	@Override
	public String toString() {
		return uri + " " + getMethodSet();
	}

	@JsonCreator
	public Authorization(Object[] array) {
		if (array.length != 2)
			throw new IllegalArgumentException("Wrong array length.");

		if (array[0] instanceof String && array[1] instanceof Integer) {
			this.uri = (String) array[0];
			this.methods = ((Integer) array[1]).byteValue();
		} else {
			throw new IllegalArgumentException("Wrong types of array elements");
		}

		this.host = "";
	}

	public Authorization(String uri, Set<Method> methods) {
		this.uri = uri;
		this.host = "";
		byte tempMethods = 0;
		for (Method m : methods) {
			tempMethods |= m.bit;
		}
		this.methods = tempMethods;
	}

	public Authorization(String uri, String host, Set<Method> methods) {
		this.uri = uri;
		this.host = host;
		byte tempMethods = 0;
		for (Method m : methods) {
			tempMethods |= m.bit;
		}
		this.methods = tempMethods;
	}

	public Set<Method> getMethodSet() {
		HashSet<Method> result = new HashSet<>();

		for (Method m : Method.values()) {

			int andresult = m.bit & methods;

			if (andresult != 0) {
				result.add(m);
			}
		}

		return result;
	}

	protected static class AuthorizationSerializer extends JsonSerializer<Authorization> {

		@Override
		public void serialize(Authorization value, JsonGenerator jgen, SerializerProvider provider)
				throws IOException {

			jgen.writeStartArray();
			jgen.writeString(value.uri);
			jgen.writeNumber(value.methods);
			jgen.writeEndArray();
		}
	}
}
