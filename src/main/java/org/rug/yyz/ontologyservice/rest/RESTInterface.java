package org.rug.yyz.ontologyservice.rest;
import org.rug.yyz.ontologyservice.ontology.OntoControlApi;
import org.rug.yyz.ontologyservice.rabbitmq.EventReceiver;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Created with IntelliJ IDEA.
 * User: yuanzhe
 * Date: 13-12-6
 * Time: 下午1:51
 * To change this template use File | Settings | File Templates.
 */

/**
 * The class for a set of REST interfaces
 */
@Path("/")
public class RESTInterface {

    /**
     * Get the classes of the environment ontology
     * @return a tree style hierarchy of classes structure
     */
    @GET
    @Path("/getClasses")
    @Produces(MediaType.TEXT_PLAIN)
    public String getClasses() {
        String result="Classes:\n\n"+OntoControlApi.printHierarchy(OntoControlApi.getRoot(),0);
        return result;
    }

    /**
     * Query the super classes, equivalent classes, subclasses and instances of a given concept
     * @param query an OWL statement describing a concept
     * @param supercls boolean value indicating if super classes of the concept is requested
     * @param equiv boolean value indicating if equivalent classes of the concept is requested
     * @param sub boolean value indicating if sub classes of the concept is requested
     * @param ins boolean value indicating if instances of the concept is requested
     * @return answer of the query in string
     */
    @GET
    @Path("/dlQuery")
    @Produces(MediaType.TEXT_PLAIN)
    public String dlQuery(@QueryParam("query") String query,
                          @QueryParam("super") String supercls,
                          @QueryParam("equiv") String equiv,
                          @QueryParam("sub") String sub,
                          @QueryParam("ins") String ins) {
        return OntoControlApi.dlQuery(query,Boolean.parseBoolean(supercls),
                Boolean.parseBoolean(equiv),
                Boolean.parseBoolean(sub),Boolean.parseBoolean(ins));
    }

    /**
     * Get the properties of the environment ontology
     * @return a tree style hierarchy of properties structure
     */
    @GET
    @Path("/getProperties")
    @Produces(MediaType.TEXT_PLAIN)
    public String getProperties() {
        return OntoControlApi.printProperties();
    }

    /**
     * Get the individuals of the environment ontology
     * @return a list of individuals concentrated in a string
     */
    @GET
    @Path("/getIndividuals")
    @Produces(MediaType.TEXT_PLAIN)
    public String getIndividuals() {
        return OntoControlApi.printIndividuals();
    }

    /**
     * Get the individuals of the environment ontology
     * @param query an OWL concept statement
     * @return a list of individuals concentrated in a special format "ind1|ind2|ind3..."
     */
    @GET
    @Path("/getTrimIndividuals")
    @Produces(MediaType.TEXT_PLAIN)
    public String getTrimIndividuals(@QueryParam("query") String query) {
        return OntoControlApi.getTrimIndividuals(query);
    }

    /**
     *
     * @param rule
     * @return
     */
    @GET
    @Path("/batchRuleGeneration")
    @Produces(MediaType.TEXT_PLAIN)
    public String batchRuleGeneration(@QueryParam("rule") String rule) {
        return OntoControlApi.batchRuleGeneration(rule);
    }

    /**
     * Start the ontology service
     */
    @GET
    @Path("/start")
    public void start() {
        EventReceiver.establishConnection();
    }

    /**
     * Delete the current ontology and rebuild a new one
     * @return result (true or false)
     */
    @GET
    @Path("/rebuildOntology")
    @Produces(MediaType.TEXT_PLAIN)
    public String rebuildOntology() {
        return OntoControlApi.rebuildOntology();
    }

    /**
     * Save the current ontology into a file in OWL format
     * @return result (true or false)
     */
    @GET
    @Path("/saveOntology")
    @Produces(MediaType.TEXT_PLAIN)
    public String saveOntology() {
        return OntoControlApi.saveOntology();
    }
}
