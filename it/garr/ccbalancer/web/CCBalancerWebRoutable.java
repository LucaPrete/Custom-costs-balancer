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
 * @see it.garr.mpbalance.CCBalancer
 * @see it.garr.mpbalance.web.serializers.LinkJSONSerializer
 * @see net.floodlightcontroller.topology.TopologyInstanceCCBalancer
 * @see net.floodlightcontroller.topology.TopologyManagerCCBalancer
 * 
 */

package it.garr.ccbalancer.web;

import net.floodlightcontroller.restserver.RestletRoutable;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class CCBalancerWebRoutable implements RestletRoutable {
    @Override
    public Restlet getRestlet(Context context) {
        Router router = new Router(context);
        router.attach("/topocosts/json", CCBalancerTopoListResource.class);
        return router;
    }

    @Override
    public String basePath() {
        return "/wm/ccbalancer";
    }
}