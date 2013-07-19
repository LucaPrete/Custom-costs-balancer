Custom costs balancer (CCBalancer) Floodlight module
====================================================

Custom costs balancer is a Floodlight module, made to setup custom costs on the OpenFlow links cached by the Floodlight
Topology module.

The Floodlight controller implements the Openflow protocol, which specifications can be found here:
 [Openflow spec](http://www.openflow.org/documents/openflow-spec-v1.0.0.pdf)

This project depends on Floodlight, which can be found here:
 [Floodlight project on GitHub](https://github.com/floodlight/floodlight).

It has been tested with Mininet, which can be found here:
 [Mininet project on GitHub](https://github.com/mininet/mininet).


License
=======

This sofware is licensed under the Apache License, Version 2.0.

Information can be found here:
 [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).


The Custom costs balancer module
================================

Using the standard Topology module of Floodlight, link costs are only set to 1 by the controller itself. Thus, best path
are just calculated considering the minimum number of hops between two OpenFlow devices.
With custom costs balancer (CCBalancer) module, users can setup via new REST API integer custom costs for each link in
each direction. Thus, using external alghoritm, a user can also provide his own metric to calculate best paths.


Installation and configuration
==============================

This project has been developed and tested on Floodlight v0.90.

The module consists of the following components:
  * it.garr.ccbalancer package
  * it.garr.ccbalancer.web package
  * it.garr.ccbalancer.web.serializers package

  * TopologyInstanceCCBalancer.java
  * TopologyManagerCCBalancer.java
  * ITopologyServiceCCBalancer.java

Eclipse
-------

Using Eclipse, after the import of the Floodlight project:
  * Copy all it.garr.ccbalancer.* packages in the project folder (outside net.floodlightcontroller)
  * Copy TopologyInstanceCCBalancer.java, TopologyManagerCCBalancer.java and ITopologyServiceCCBalancer
    in net.floodlightcontroller.topology
  * Modify the file src/main/resources/META-INF/services/net.floodlightcontroller.core.module.IFloodlightModule
    ** net.floodlightcontroller.topology.TopologyManagerCCBalancer instead of
       net.floodlightcontroller.topology.TopologyManager
    ** add it.garr.ccbalancer.CCBalancer
  * Modify the file src/main/resources/floodlight.properties
    ** net.floodlightcontroller.topology.TopologyManagerCCBalancer instead of
       net.floodlightcontroller.topology.TopologyManager
    ** add it.garr.ccbalancer.CCBalancer

Runnable file
-------------

For production environment, a jar version of the module is downloadable as well from the root directory of this
GitHub repository.

Alternatively, it is possible to create your own ``ccbalancer.jar`` with the compiled files from this project.

According to Floodlight command sintax, you can integrate the jar file to your Floodlight installation running the
command:
```
java -cp floodlight.jar:ccbalancer.jar net.floodlightcontroller.core.Main -cf floodlight.properties
```

The parameters specified have the following meaning:
 * ``floodlight.properties`` is the file specifying the properties for the running instance of Floodlight,
   it is configred to start the KHopMetric module provided with this project.
