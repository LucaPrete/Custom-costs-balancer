/**
 * Copyright (C) 2013 Luca Prete, Simone Visconti, Andrea Biancini, Fabio Farina - www.garr.it - Consortium GARR
 * 
 * This is an extended, modified version of the original TopologyInstance
 * file provided with Floodlight 0.90
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
 * Implementation of the Floodlight TopologyInstance for CCBalancer service.
 * A representation of a network topology.  Used internally by 
 * {@link TopologyManager}
 * 
 * @author Luca Prete <luca.prete@garr.it>
 * @author Andrea Biancini <andrea.biancini@garr.it>
 * @author Fabio Farina <fabio.farina@garr.it>
 * @author Simone Visconti<simone.visconti.89@gmail.com>
 * 
 * @version 0.90
 * @see it.garr.mpbalance.CCBalancer
 * @see it.garr.mpbalance.ICCBalancerListener
 * @see it.garr.mpbalance.ICCBalancerService
 * @see it.garr.mpbalance.web.CCBalancerTopoListResource
 * @see it.garr.mpbalance.web.CCBalancerWebRoutable
 * @see it.garr.mpbalance.web.serializers.LinkJSONSerializer
 * @see net.floodlightcontroller.topology.TopologyManagerCCBalancer
 * 
 */

package net.floodlightcontroller.topology;

import it.garr.ccbalancer.ICCBalancerService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import net.floodlightcontroller.core.annotations.LogMessageCategory;
import net.floodlightcontroller.routing.BroadcastTree;
import net.floodlightcontroller.routing.Link;
import net.floodlightcontroller.routing.Route;
import net.floodlightcontroller.routing.RouteId;
import net.floodlightcontroller.util.LRUHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@LogMessageCategory("Network Topology")
public class TopologyInstanceCCBalancer extends TopologyInstance {

    public static final short LT_SH_LINK = 1;
    public static final short LT_BD_LINK = 2;
    public static final short LT_TUNNEL  = 3; 

    public static final int MAX_LINK_WEIGHT = 10000;
    public static final int MAX_PATH_WEIGHT = Integer.MAX_VALUE - MAX_LINK_WEIGHT - 1;
    public static final int PATH_CACHE_SIZE = 1000;

    protected ICCBalancerService mpbalance;

    protected static Logger log = LoggerFactory.getLogger(TopologyInstanceCCBalancer.class);

    public TopologyInstanceCCBalancer(ICCBalancerService mpbalance) {
        this.switches = new HashSet<Long>();
        this.switchPorts = new HashMap<Long, Set<Short>>();
        this.switchPortLinks = new HashMap<NodePortTuple, Set<Link>>();
        this.broadcastDomainPorts = new HashSet<NodePortTuple>();
        this.tunnelPorts = new HashSet<NodePortTuple>();
        this.blockedPorts = new HashSet<NodePortTuple>();
        this.blockedLinks = new HashSet<Link>();
        this.mpbalance = mpbalance;
    }
    
    public TopologyInstanceCCBalancer(Map<Long, Set<Short>> switchPorts,
            Map<NodePortTuple, Set<Link>> switchPortLinks, ICCBalancerService mpbalance)
	{
		this.switches = new HashSet<Long>(switchPorts.keySet());
		this.switchPorts = new HashMap<Long, Set<Short>>(switchPorts);
		this.switchPortLinks = new HashMap<NodePortTuple, 
		                           Set<Link>>(switchPortLinks);
		this.broadcastDomainPorts = new HashSet<NodePortTuple>();
		this.tunnelPorts = new HashSet<NodePortTuple>();
		this.blockedPorts = new HashSet<NodePortTuple>();
		this.blockedLinks = new HashSet<Link>();
        this.mpbalance = mpbalance;

		clusters = new HashSet<Cluster>();
		switchClusterMap = new HashMap<Long, Cluster>();
	}
    
    public TopologyInstanceCCBalancer(Map<Long, Set<Short>> switchPorts,
            Set<NodePortTuple> blockedPorts,
            Map<NodePortTuple, Set<Link>> switchPortLinks,
            Set<NodePortTuple> broadcastDomainPorts,
            Set<NodePortTuple> tunnelPorts, ICCBalancerService mpbalance){

// copy these structures
this.switches = new HashSet<Long>(switchPorts.keySet());
this.switchPorts = new HashMap<Long, Set<Short>>();
this.mpbalance = mpbalance;
for(long sw: switchPorts.keySet()) {
this.switchPorts.put(sw, new HashSet<Short>(switchPorts.get(sw)));
}

this.blockedPorts = new HashSet<NodePortTuple>(blockedPorts);
this.switchPortLinks = new HashMap<NodePortTuple, Set<Link>>();
for(NodePortTuple npt: switchPortLinks.keySet()) {
this.switchPortLinks.put(npt, 
                     new HashSet<Link>(switchPortLinks.get(npt)));
}
this.broadcastDomainPorts = new HashSet<NodePortTuple>(broadcastDomainPorts);
this.tunnelPorts = new HashSet<NodePortTuple>(tunnelPorts);

blockedLinks = new HashSet<Link>();
clusters = new HashSet<Cluster>();
switchClusterMap = new HashMap<Long, Cluster>();
destinationRootedTrees = new HashMap<Long, BroadcastTree>();
clusterBroadcastTrees = new HashMap<Long, BroadcastTree>();
clusterBroadcastNodePorts = new HashMap<Long, Set<NodePortTuple>>();
pathcache = new LRUHashMap<RouteId, Route>(PATH_CACHE_SIZE);
}
    
    
    
	@Override
	protected void calculateShortestPathTreeInClusters() {
		pathcache.clear();
        destinationRootedTrees.clear();
        
        Map<Link, Integer> linkCost = mpbalance.getLinkCost();
        
        for(Cluster c: clusters) {
            for (Long node : c.links.keySet()) {
                BroadcastTree tree = dijkstra(c, node, linkCost, true);
                destinationRootedTrees.put(node, tree);
            }
        }
	}
	
	@Override
	protected BroadcastTree dijkstra(Cluster c, Long root, 
            Map<Link, Integer> linkCost,
            boolean isDstRooted) {
				HashMap<Long, Link> nexthoplinks = new HashMap<Long, Link>();
				//HashMap<Long, Long> nexthopnodes = new HashMap<Long, Long>();
				HashMap<Long, Integer> cost = new HashMap<Long, Integer>();
				int w;
				
				for (Long node: c.links.keySet()) {
					nexthoplinks.put(node, null);
					//nexthopnodes.put(node, null);
					cost.put(node, MAX_PATH_WEIGHT);
				}

				PriorityQueue<NodeDist> nodeq = new PriorityQueue<NodeDist>();
				nodeq.add(new NodeDist(root, 0));
				
				cost.put(root, 0);
				while (nodeq.peek() != null) {
					NodeDist n = nodeq.poll();
					Long cnode = n.getNode();
					int cdist = n.getDist();
					if (cdist >= MAX_PATH_WEIGHT) break;
					for (Link link: c.links.get(cnode)) {
						Long neighbor;
						
						if (isDstRooted == true) neighbor = link.getSrc();
						else neighbor = link.getDst();
						
						// links directed toward cnode will result in this condition
						// if (neighbor == cnode) continue;
						
						if (linkCost == null || linkCost.get(link)==null) w = 1;
						else w = linkCost.get(link);
						int ndist = cdist + w; 
						if (ndist < cost.get(neighbor)) {
						cost.put(neighbor, ndist);
						nexthoplinks.put(neighbor, link);
						//nexthopnodes.put(neighbor, cnode);
						nodeq.add(new NodeDist(neighbor, ndist));
						}
					}
				}
				BroadcastTree ret = new BroadcastTree(nexthoplinks, cost);
				return ret;
			}
}