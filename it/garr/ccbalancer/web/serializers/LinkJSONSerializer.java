package it.garr.ccbalancer.web.serializers;

import java.io.IOException;

import net.floodlightcontroller.routing.Link;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.openflow.util.HexString;

/**
 * Class that serializes the Link type of the CCBalancer service.
 *
 * @author Luca Prete <luca.prete@garr.it>
 * @author Andrea Biancini <andrea.biancini@garr.it>
 * @author Fabio Farina <fabio.farina@garr.it>
 * @author Simone Visconti <simone.visconti.89@gmail.com>
 * 
 * @version 0.90
 * @see org.codehaus.jackson.map.JsonSerializer
 *
 */

public class LinkJSONSerializer extends JsonSerializer<Link> {

    @Override
    public void serialize(Link link, JsonGenerator jGen, SerializerProvider sProvider)
    		throws IOException, JsonProcessingException {
    	
        jGen.writeStartObject();
        
        jGen.writeStringField("sourceSwitch", HexString.toHexString(link.getSrc()));
        jGen.writeNumberField("sourcePort", link.getSrcPort());
        jGen.writeStringField("destinationSwitch", HexString.toHexString(link.getDst()));
        jGen.writeNumberField("destinationPort", link.getDstPort());
        
        jGen.writeEndObject();
    }

    @Override
    public Class<Link> handledType() {
        return Link.class;
    }

}