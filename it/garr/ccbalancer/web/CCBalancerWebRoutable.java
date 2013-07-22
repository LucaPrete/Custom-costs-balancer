package it.garr.ccbalancer.web;

import net.floodlightcontroller.restserver.RestletRoutable;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

/**
 * Class to describe REST routes for the CCBalancer service.
 *
 * @author Luca Prete <luca.prete@garr.it>
 * @author Andrea Biancini <andrea.biancini@garr.it>
 * @author Fabio Farina <fabio.farina@garr.it>
 * @author Simone Visconti <simone.visconti.89@gmail.com>
 * 
 * @version 0.90
 * @see net.floodlightcontroller.restserver.RestletRoutable
 * @see it.garr.ccbalancer.web.CCBalancerTopoListResource
 *
 */

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