package it.garr.ccbalancer;

/**
 * Implementation of the Floodlight CCBalancer service.
 *
 * @author Luca Prete <luca.prete@garr.it>
 * @author Andrea Biancini <andrea.biancini@garr.it>
 * @author Fabio Farina <fabio.farina@garr.it>
 * @author Simone Visconti<simone.visconti.89@gmail.com>
 * 
 * @version 0.90
 */

public interface ICCBalancerListener {
    /**
     * Happens when the switch clusters are recomputed
     */
    void costChanged();
}
