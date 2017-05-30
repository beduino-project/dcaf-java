import de.unibremen.beduino.dcaf.AllInterfacesCoapServer;
import de.unibremen.beduino.dcaf.ClientAuthorizationManager;
import de.unibremen.beduino.dcaf.PskRepository;
import de.unibremen.beduino.dcaf.ServerAuthorizationManager;
import de.unibremen.beduino.dcaf.datastructures.AccessRequest;
import de.unibremen.beduino.dcaf.datastructures.TicketGrantMessage;
import de.unibremen.beduino.dcaf.staticimpl.LocalServerAuthorizationManager;
import de.unibremen.beduino.dcaf.staticimpl.PermissionFilterStaticImpl;
import de.unibremen.beduino.dcaf.utils.Tuple;
import de.unibremen.beduino.dcaf.Utils;
import org.apache.commons.codec.binary.Hex;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Optional;

public class ExampleServer {

    private static Logger logger = LoggerFactory.getLogger(ExampleServer.class);

    public static void main(String[] args) {
    	/* Create CoAP Server listening on all interfaces (IPv6 & IPv4) */
        AllInterfacesCoapServer server = new AllInterfacesCoapServer();

        ClientAuthorizationManager cam = createCamWithLocalSam();

		/* Add CoAP Resource to process incoming requests */
        server.add(new CoapResource("client-authorize"){

            {
            	/* Set title/description for the CoAP Resource */
                getAttributes().setTitle("DCAF Client Authorization Manager");
            }

            @Override
            public void handlePOST(CoapExchange exchange) {
                byte[] requestPayload = exchange.getRequestPayload();

                /* Deserialize incoming requests from CBOR to Java Objects */
                Optional<AccessRequest> request = Utils.deserializeCbor(requestPayload, AccessRequest.class);
                if (!request.isPresent()) {
                    logger.error("Error 500 - deserializeCbor failed");
                    exchange.respond(CoAP.ResponseCode.BAD_REQUEST);
                    return;
                }

                /* Do something here (or even before deserialization to check if the client is allowed to
                 * get what he requested */

                /* Get the ticket grant message by calling cam.process.
                 * The CAM is going to contact the SAM to get the TicketGrantMessage */
                TicketGrantMessage grant = cam.process(request.get());

                /* Serialize the grant answer and return it to the client. */
                Optional<byte[]> answer = Utils.serializeCbor(grant);
                if (answer.isPresent()) {
                    logger.debug("respond: h'" + Hex.encodeHexString(answer.get()) + "'");
                    exchange.respond(CoAP.ResponseCode.CONTENT, answer.get(), Utils.getDcafMediaType());
                } else {
                    logger.error("Error 500 - serializeCbor failed");
                    exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
                }

            }
        });
    }


    private static ClientAuthorizationManager createCamWithLocalSam() {
        ClientAuthorizationManager cam = new ClientAuthorizationManager();
        cam.setIdentifier("Local CAM");

        HashMap<String, byte[]> pskMap = new HashMap<String, byte[]>() {{
            put("temperatureSensor1.example.com", "topSecretKey".getBytes());
            put("temperatureSensor1.example.com", "anotherTopSecretKey".getBytes());
            put("lightbulb1.example.com", new byte[]{0x11, 0x22, 0x33, 0x44, 0x55});
            put("lightbulb2.example.com", new byte[]{0x66, 0x77, 0x00, 0x11, 0x22});
        }};

        PskRepository psks = hostname -> Optional.ofNullable(pskMap.get(hostname));

        ServerAuthorizationManager localSam = new LocalServerAuthorizationManager(new PermissionFilterStaticImpl(), psks);

        cam.registerSamLocator(samUrl -> {
            if(samUrl.getHost().equals("sam.example.com")) {
                return Optional.of(Tuple.of(1,localSam));
            } else {
                return Optional.empty();
            }
        });

        return cam;
    }

}
