package de.unibremen.beduino.dcaf.datastructures;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.util.Date;

/**
 * von C zu CAM
 *
 * @author Connor Lanigan
 * @author Sven Höper
 */
@JsonSerialize(using = AccessRequest.AccessRequestSerializer.class)
public class AccessRequest {
    public final String samUrl;
    public final Authorization[] sai;
    public final Date timestamp;

    @JsonCreator
    public AccessRequest(@JsonProperty("0") String samUrl,
                         @JsonProperty("1") Authorization[] sai,
                         @JsonProperty("5") Date timestamp) {
        this.samUrl = samUrl;
        this.sai = sai;
        this.timestamp = timestamp;
    }

    protected static class AccessRequestSerializer extends JsonSerializer<AccessRequest> {

        @Override
        public void serialize(AccessRequest value, JsonGenerator jgen, SerializerProvider provider)
                throws IOException {

            jgen.writeFieldId(0);
            jgen.writeString(value.samUrl);
            jgen.writeFieldId(1);
            jgen.writeObject(value.sai);
            jgen.writeFieldId(5);
            jgen.writeNumber(value.timestamp.getTime()/1000);
        }
    }
}
