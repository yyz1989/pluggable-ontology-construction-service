package org.rug.yyz.ontologyservice.rest;

import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

/**
 * Created with IntelliJ IDEA.
 * User: yuanzhe
 * Date: 13-12-8
 * Time: 下午2:41
 * To change this template use File | Settings | File Templates.
 */

/**
 * Entrypoint of the Web service
 */
@ApplicationPath("/ontology")
public class WebApp extends ResourceConfig {
    public WebApp() {
        packages("org.rug.yyz.ontologyservice.rest");
    }
}