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
 * @see it.garr.mpbalance.web.CCBalancerTopoListResource
 * @see it.garr.mpbalance.web.CCBalancerWebRoutable
 * @see it.garr.mpbalance.web.serializers.LinkJSONSerializer
 * @see net.floodlightcontroller.topology.TopologyInstanceCCBalancer
 * @see net.floodlightcontroller.topology.TopologyManagerCCBalancer
 * 
 */

package it.garr.ccbalancer;

import it.garr.ccbalancer.web.CCBalancerWebRoutable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.linkdiscovery.ILinkDiscovery;
import net.floodlightcontroller.linkdiscovery.ILinkDiscovery.LDUpdate;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.routing.Link;
import net.floodlightcontroller.topology.ITopologyListener;
import net.floodlightcontroller.topology.ITopologyService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CCBalancer implements IFloodlightModule, ICCBalancerService, ITopologyListener{
	
	protected static Logger logger = LoggerFactory.getLogger(CCBalancer.class);
	
	// Service references
	protected IRestApiService restApi = null;
	protected IFloodlightProviderService floodlightProvider = null;
	protected ITopologyService topology = null;
	
	// Data structures for caching algorithm results
	protected Map<Link, Integer> linkCost = null;
	
	// Modules that listen to our updates
    protected ArrayList<ICCBalancerListener> costAware;
    
	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
	    l.add(ICCBalancerService.class);
	    return l;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		Map<Class<? extends IFloodlightService>, IFloodlightService> m = new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
	    m.put(ICCBalancerService.class, this);
	    return m;
	}
	
	public void topologyChanged() {
		for (LDUpdate update : topology.getLastLinkUpdates()) {
			
			//If update is LINK_UPDATE and link is not in topology cost
			if (update.getOperation().equals(ILinkDiscovery.UpdateOperation.LINK_UPDATED)){
				// Initialize linkupdate with the link update.
				Link linkUpdate = new Link(update.getSrc(), update.getSrcPort(), update.getDst(), update.getDstPort());
				// If link is new, cost is anytime "1"
				if(!linkExists(linkUpdate)){
					logger.info("Found  " + linkUpdate +". ");
					linkCost.put(linkUpdate, 1);
				}
			}
			// if link exist and update is LINK_REMOVED
			else if (update.getOperation().equals(ILinkDiscovery.UpdateOperation.LINK_REMOVED)) {
				// Initialize linkupdate with the link update.
				Link linkUpdate = new Link(update.getSrc(), update.getSrcPort(), update.getDst(), update.getDstPort());
            	linkCost.remove(linkUpdate);
            	logger.info("Removed Link"+ linkUpdate +" from topology - cost");
			}
		}
	}
	
	
	private Boolean linkExists(Link link){
		
		if(linkCost.containsKey(link))
			return true;
			return false;
	}
	
	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
	    l.add(IFloodlightProviderService.class);
	    l.add(IRestApiService.class);
	    return l;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
		restApi = context.getServiceImpl(IRestApiService.class);
		topology = context.getServiceImpl(ITopologyService.class);
		costAware = new ArrayList<ICCBalancerListener>();
		linkCost = new HashMap<Link, Integer>();
	}

	@Override
	public void startUp(FloodlightModuleContext context) {
		if (topology != null) topology.addListener(this);
		if (restApi != null) restApi.addRestletRoutable(new CCBalancerWebRoutable());
	}

	@Override
	public HashMap<Link, Integer> getLinkCost() {
		return (HashMap<Link, Integer>) linkCost;
	}
	
	@Override
	public void setLinkCost(HashMap<Link, Integer> linkCost) {
		for(Link link : linkCost.keySet()){
			if (linkExists(link)){
				this.linkCost.put(link, linkCost.get(link));
			}
		}
		informListeners();
	}

	@Override
	public void addListener(ICCBalancerListener listener) {
		costAware.add(listener);
	}
	
	public void informListeners() {
        for(int i=0; i<costAware.size(); ++i) {
            ICCBalancerListener listener = costAware.get(i);
            listener.costChanged();
        }
    }
	
    
	
}