pluggable-ontology-construction-service
=======================================

### Introduction

This is a pluggable extension for the smart envrionment solution in the EU GreenerBuildings project. 

The context model of the original solution is a spatial model, i.e., all the objects are correlated 
based on the inclusion of locations. For example, an actuator
named room1.blinds1 indicates that it is the Blinds No. 1 inside the Room No. 1.
It also has an attribute location stating floor3.room1, which denotes the room this
actuator stays in is also contained in the Floor No. 3. Hence, all the sensors and
actuators are organized hierarchically according to their locations.

However, this spatial model exhibits weakness in terms of relationships and reasoning:
it has a rather limited expressivity for relationships between objects (only spatial
inclusion), cannot derive new facts from existing context data, and does not support
consistency verification. In contrast, it is believed that ontology-based models are
specialized in coping with such requirements decently. 

Since the GreenerBuildings has provided a complete solution, it is not trivial
and realistic to reform the entire system. Compared with reconstruction, a better
idea is extension: in order to minimize the modification to the original architecture
and implementation, a proposal comes up by developing a hybrid solution where the
original context model remains unchanged, and an independent and pluggable component
operates an ontology to manage the context knowledge and provide services
to other components, which leads to the implementation of this "pluggable ontology construction service".

The demonstrator of this plugin is implemented as a Web service with Jersey REST API and a Jetty server is responsible
for running it. The communication between this plugin and other smart environment components (including Context and Rule
Maintenance Engine in GreenerBuildings' implementation) are based on messaging service RabbitMQ. The ontology of the 
environment is generated dynamically according to all the incoming state changes of the environment. Since message queue
ensures integrity and order, the ontology of the smart environment will be always consistent with the current environment
state. Then all the information contained in this ontology can be used for other further usages such as activity recognition,
information enrichment, query and search, etc.

The author is not authorized to share the source code of the other components from GreenerBuildings project for running 
a complete smart environment. Please contact the author for further research and questions.

### Requirements
    * JDK (>=6)
    * Maven
    * RabbitMQ Server

### Installation
    git clone this repository
    cd to the root directory of this repository
    mvn jetty:run
