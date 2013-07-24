/**
 * Copyright (C) 2013 Luca Prete, Simone Visconti, Andrea Biancini, Fabio Farina - www.garr.it - Consortium GARR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * @author Luca Prete <luca.prete@garr.it>
 * @author Andrea Biancini <andrea.biancini@garr.it>
 * @author Fabio Farina <fabio.farina@garr.it>
 * @author Simone Visconti<simone.visconti.89@gmail.com>
 * 
 * @version 0.90
 * @see it.garr.mpbalance.ICCBalancerListener
 * @see it.garr.mpbalance.ICCBalancerService
 * @see it.garr.mpbalance.CCBalancer
 * @see it.garr.mpbalance.web.CCBalancerWebRoutable
 * @see it.garr.mpbalance.web.serializers.LinkJSONSerializer
 * @see net.floodlightcontroller.topology.TopologyInstanceCCBalancer
 * @see net.floodlightcontroller.topology.TopologyManagerCCBalancer
 * 
 */

package it.garr.ccbalancer.web;

import it.garr.ccbalancer.ICCBalancerService;

import java.io.IOException;
import java.util.HashMap;

import net.floodlightcontroller.routing.Link;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.openflow.util.HexString;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CCBalancerTopoListResource extends ServerResource {
	
	protected Logger logger = LoggerFactory.getLogger(CCBalancerTopoListResource.class);	
	public Link NewLink = null;
	
	
	
	@Get("json")
    public HashMap<Link, Integer> retrieve() {
        ICCBalancerService service = (ICCBalancerService)getContext().getAttributes().get(ICCBalancerService.class.getCanonicalName());
        HashMap<Link, Integer> l = new HashMap<Link, Integer>();
        l.putAll(service.getLinkCost());
        return l;
	}
	
    @Post
    public String handlePost(String fmJson) throws IOException {
       	ICCBalancerService mpbalance = (ICCBalancerService) getContext().getAttributes().get(ICCBalancerService.class.getCanonicalName());
       	HashMap<Link, Integer> linkCost;
        try {
            linkCost = jsonToLinkCost(fmJson);
        } catch (IOException e) {
        	logger.error("");  //FARE
        	linkCost=new HashMap<Link, Integer>();
            return "{\"status\" : \"Error!!!!Could not parse new cost, see log for details.\"}";
        }
        String status = null;
            // add rule to firewall
            mpbalance.setLinkCost(linkCost);
            status = "Cost added";
        return ("{\"status\" : \"" + status + "\"}");
    }

	private HashMap<Link, Integer> jsonToLinkCost(String fmJson) throws IOException {
		HashMap<Link, Integer> linkCost = new HashMap<Link, Integer>();
        MappingJsonFactory f = new MappingJsonFactory();
        JsonParser jp;

        try {
            jp = f.createJsonParser(fmJson);
        } catch (JsonParseException e) {
            throw new IOException(e);
        }
        
        jp.nextToken();

        if (jp.getText() != "[") {
        	
            throw new IOException("Expected START_ARRAY");
        }
        jp.nextToken();
        while (jp.getCurrentToken() != JsonToken.END_ARRAY ) {
        	
        	jp.nextToken();
            String n = null;
        	long src = 0;
        	short outport = 0;
        	long dst = 0;
        	short inport = 0;
        	Integer cost = 0;
         	
        	n = jp.getCurrentName();
        	// Adding source
        	if (n == "src"){
        		jp.nextToken();
        		src = HexString.toLong(jp.getText());
        	} else throw new IOException("Expected source");
        	
        	jp.nextToken();
        	 n = jp.getCurrentName();	
        	// Adding outport
        	if (n == "outPort"){
        		jp.nextToken();
        		outport = Short.valueOf(jp.getText());
        	} else throw new IOException("Expected outPort");        	
        	
        	jp.nextToken();
        	n = jp.getCurrentName();
        	// Adding Cost
        	if(n == "cost"){
        		jp.nextToken();
        		cost = Integer.valueOf(jp.getText());
        	} else throw new IOException("Expected cost");
        	
        	jp.nextToken();
        	n = jp.getCurrentName();   	
        	// Adding destination
        	if (n == "dst"){
        		jp.nextToken();
        		dst = HexString.toLong(jp.getText());
        	} else throw new IOException("Expected destination");
        	
        	jp.nextToken();
        	n = jp.getCurrentName();
        	// Adding source
        	if (n == "inPort"){
        		jp.nextToken();
        		inport = Short.valueOf(jp.getText());
        	} else throw new IOException("Expected inPort");
        	
        	jp.nextToken();
        	jp.nextToken();
        	linkCost.put(new Link(src, outport, dst, inport), cost);
        }
        return linkCost;
	}	
}
