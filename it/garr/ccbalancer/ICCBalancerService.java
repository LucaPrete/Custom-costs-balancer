package it.garr.ccbalancer;


import java.util.HashMap;

import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.routing.Link;

/**
 * Public interface for the Floodlight CCBalancer service.
 *
 * @author Luca Prete <luca.prete@garr.it>
 * @author Andrea Biancini <andrea.biancini@garr.it>
 * @author Fabio Farina <fabio.farina@garr.it>
 * @author Simone Visconti<simone.visconti.89@gmail.com>
 * 
 * @version 0.90
 * @see net.floodlightcontroller.core.module.IFloodlightService
 *
 */

public interface ICCBalancerService extends IFloodlightService {
	
	public void addListener(ICCBalancerListener listener);
	
	public HashMap<Link, Integer> getLinkCost();
	    
	public void setLinkCost(HashMap<Link, Integer> linkCost);
}