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
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

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
		try {
			JSONArray jarray = new JSONArray(fmJson);
			
			HashMap<Link, Integer> linkCost = new HashMap<Link, Integer>();
			
        	long src = 0;
        	short outport = 0;
        	long dst = 0;
        	short inport = 0;
        	int cost = 0;
		
			for (int i=0 ; i < jarray.length ; i++){
					JSONObject jobj = jarray.getJSONObject(i);
					src = HexString.toLong(jobj.getString("src"));
					outport = (short) jobj.getInt("outPort");
					dst = HexString.toLong(jobj.getString("dst"));
					inport = (short) jobj.getInt("inPort");
					cost = jobj.getInt("cost");
					linkCost.put(new Link(src, outport, dst, inport), cost);
			}
		}catch(JSONException e) {
			if(!fmJson.startsWith("[]")) throw new IOException("Expected START_ARRAY");
			else if (e.getMessage().contains("src") && e.getMessage().endsWith("not found.")) throw new IOException("Expected source");
			else if (e.getMessage().contains("outPort") && e.getMessage().endsWith("not found."))throw new IOException("Expected outPort");
			else if (e.getMessage().contains("cost") && e.getMessage().endsWith("not found.")) throw new IOException("Expected cost");
			else if (e.getMessage().contains("dst") && e.getMessage().endsWith("not found.")) throw new IOException("Expected destination");
			else if (e.getMessage().contains("inPort") && e.getMessage().endsWith("not found.")) throw new IOException("Expected inPort");
			System.out.println(e.getMessage());
		}
		
        return linkCost;
	}	
}
