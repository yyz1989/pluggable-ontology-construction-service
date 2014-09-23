package org.rug.yyz.ontologyservice.ontology;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.*;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: yuanzhe
 * Date: 13-11-26
 * Time: 下午1:17
 * To change this template use File | Settings | File Templates.
 */
public class OntoControlApi {
    private static OntoControlApi ourInstance = new OntoControlApi();
    private static Logger log;
    private static File file;
    private static OWLOntologyManager manager;
    private static OWLOntology smenvOntology;
    private static IRI documentIRI, ontologyIRI;
    private static OWLDataFactory factory;
    private static OWLReasonerFactory reasonerFactory;
    private static OWLReasoner reasoner;
    private static PrefixManager pm;
    private static ShortFormProvider shortFormProvider;
    private static DLQueryEngine dlQueryEngine;

    private static OWLClass root;
    private static OWLClass sensor;
    private static OWLClass actuator;
    private static OWLObjectProperty isContainedIn;
    private static OWLObjectProperty containsComponent;
    private static OWLDataProperty isControllable;
    private static OWLDataProperty hasState;
    private static OWLDataProperty isSlow;
    private static OWLDataProperty hasID;
    private static OWLDatatype boolType;
    private static OWLDatatype intType;
    private static OWLDatatype stringType;
    private static OWLDataUnionOf intOrString;

    /**
     * Initialize the skeleton of the ontology and a set of static variables required to manipulate it
     */
    private OntoControlApi() {
        file=new File("onto/newontology.owl");
        log= Logger.getLogger("OntologyLogs");
        manager= OWLManager.createOWLOntologyManager();
        factory = manager.getOWLDataFactory();
        ontologyIRI = IRI.create("http://www.semanticweb.org/yuanzhe/ontologies/2013/9/SmartEnvironment");
        documentIRI=IRI.create(file.toURI());
        try{
            smenvOntology=manager.createOntology(ontologyIRI);
            log.config("Ontology created");
        }
        catch (OWLOntologyCreationException e) {
            log.severe("Unable to create ontology");
            return;
        }
        manager.addIRIMapper(new SimpleIRIMapper(ontologyIRI,documentIRI));
        reasonerFactory=new PelletReasonerFactory();
        reasoner=reasonerFactory.createReasoner(smenvOntology);
        pm=new DefaultPrefixManager(ontologyIRI.toString()+"#");
        shortFormProvider=new SimpleShortFormProvider();
        dlQueryEngine=new DLQueryEngine(shortFormProvider);

        // create classes for sensors and actuators
        root=factory.getOWLThing();
        sensor=factory.getOWLClass(":SENSOR",pm);
        actuator=factory.getOWLClass(":ACTUATOR",pm);

        // create data types
        boolType=factory.getBooleanOWLDatatype();
        intType=factory.getIntegerOWLDatatype();
        stringType=factory.getOWLDatatype(OWL2Datatype.XSD_STRING.getIRI());
        intOrString=factory.getOWLDataUnionOf(intType,stringType);

        // create properties for sensors and actuators
        isContainedIn=factory.getOWLObjectProperty(":isContainedIn", pm);
        containsComponent=factory.getOWLObjectProperty(":containsComponent", pm);
        hasState=factory.getOWLDataProperty(":hasState", pm);
        isControllable=factory.getOWLDataProperty(":isControllable", pm);
        isSlow=factory.getOWLDataProperty(":isSlow", pm);
        hasID=factory.getOWLDataProperty(":hasID",pm);

        // create constraints for properties
        OWLDataHasValue controllableHasValueTrue=factory.getOWLDataHasValue(isControllable,
                factory.getOWLLiteral(true));
        OWLDataHasValue controllableHasValueFalse=factory.getOWLDataHasValue(isControllable,
                factory.getOWLLiteral(false));
        OWLEquivalentClassesAxiom controllableThings=factory.getOWLEquivalentClassesAxiom(actuator,
                controllableHasValueTrue);
        OWLEquivalentClassesAxiom uncontrollableThings=factory.getOWLEquivalentClassesAxiom(sensor,
                controllableHasValueFalse);
        OWLInverseObjectPropertiesAxiom containAx=factory.getOWLInverseObjectPropertiesAxiom(containsComponent,
                isContainedIn);
        OWLTransitiveObjectPropertyAxiom transIsContainedIn=factory.getOWLTransitiveObjectPropertyAxiom(isContainedIn);
        OWLTransitiveObjectPropertyAxiom transContainsComponent=
                factory.getOWLTransitiveObjectPropertyAxiom(containsComponent);

        // commit all the changes, the skeleton of an ontology for a smart environment will be created
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        axioms.add(factory.getOWLSubClassOfAxiom(sensor,root));
        axioms.add(factory.getOWLSubClassOfAxiom(actuator,root));
        axioms.add(controllableThings);
        axioms.add(uncontrollableThings);
        axioms.add(containAx);
        axioms.add(transContainsComponent);
        axioms.add(transIsContainedIn);
        axioms.add(factory.getOWLDataPropertyRangeAxiom(hasID, intType));
        axioms.add(factory.getOWLFunctionalDataPropertyAxiom(hasID));
        axioms.add(factory.getOWLDataPropertyRangeAxiom(isControllable, boolType));
        axioms.add(factory.getOWLFunctionalDataPropertyAxiom(isControllable));
        axioms.add(factory.getOWLDataPropertyRangeAxiom(isSlow, boolType));
        axioms.add(factory.getOWLFunctionalDataPropertyAxiom(isSlow));
        axioms.add(factory.getOWLDataPropertyRangeAxiom(hasState, intOrString));
        axioms.add(factory.getOWLFunctionalDataPropertyAxiom(hasState));
        manager.addAxioms(smenvOntology,axioms);

        // initialize reasoner for the ontology
        reasoner.precomputeInferences();
        reasoner.dispose();
        System.out.println("Ontology created at "+System.currentTimeMillis());
    }

    /**
     * Create a class and corresponding class assertions for a new individual
     * @param indName name of the individual received from a remote service in a form of "Room1.Lamp1"
     */
    public static void classAssertion(String indName) {
        Pattern pattern=Pattern.compile("[0-9]");
        Matcher matcher=pattern.matcher(indName);
        String className;

        // create a class according to the name of the individual (e.g., ROOM)
        if(matcher.find()) {
            className=indName.substring(0,matcher.start());
            className=className.toUpperCase();
        }
        else className=indName.toUpperCase();
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        OWLClass cls=factory.getOWLClass(className, pm);
        axioms.add(factory.getOWLSubClassOfAxiom(cls,root));
        axioms.add(factory.getOWLClassAssertionAxiom(cls,factory.getOWLNamedIndividual(indName,pm)));
        manager.addAxioms(smenvOntology,axioms);
        log.config("Class assertion of "+indName+" added");
    }

    /**
     * Create a class and corresponding class assertions for a new individual if the name of the individual is not
     * described in one single string
     * @param siname service name of the variable received from a remote service in a form of "Room1"
     * @param varname variable name of the variable received from a remote service in a form of "Lamp1"
     */
    public static void classAssertion(String siname, String varname) {
        String indName = siname + "." + varname;
        classAssertion(indName);
    }

    /**
     * For a new environment variable received from a remote service, create it as an individual in the ontology and
     * create corresponding assertions for all its attributes
     * @param var an environment variable as an instance of Variable class
     */
    public static void addVariable(Variable var) {
        OWLNamedIndividual ind=factory.getOWLNamedIndividual(var.getSiname()+"."+var.getName(),pm);

        // if the variable with the same name exists in the ontology, delete the old one from the ontology first
        if(smenvOntology.containsIndividualInSignature(ind.getIRI())) {
            deleteVariable(var.getSiname(),var.getName());
        }
        OWLNamedIndividual sind=factory.getOWLNamedIndividual(var.getSiname(),pm);
        classAssertion(var.getSiname(),var.getName());
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        axioms.add(factory.getOWLDataPropertyAssertionAxiom(hasID, ind, var.getId()));
        axioms.add(factory.getOWLDataPropertyAssertionAxiom(isControllable, ind, var.isControllable()));
        axioms.add(factory.getOWLDataPropertyAssertionAxiom(isSlow,ind,var.isSlow()));
        axioms.add(factory.getOWLObjectPropertyAssertionAxiom(containsComponent,sind,ind));
        manager.addAxioms(smenvOntology,axioms);
        addLocationAxioms(var.getLocation());
        log.config("Individual "+ind.getIRI().getFragment()+" added");
    }

    /**
     * Delete a variable, i.e., an individual from the ontology by removing all its assertions
     * @param siname service name of the variable received from a remote service in a form of "Room1"
     * @param varname variable name of the variable received from a remote service in a form of "Lamp1"
     */
    public static void deleteVariable(String siname, String varname) {
        OWLEntityRemover remover=new OWLEntityRemover(manager, Collections.singleton(smenvOntology));
        OWLNamedIndividual ind=factory.getOWLNamedIndividual(siname+"."+varname,pm);
        ind.accept(remover);
        manager.applyChanges(remover.getChanges());
        log.config("Individual "+siname+"."+varname+" deleted");
        remover.reset();
    }

    /**
     * Delete a variable, i.e., an individual from the ontology by removing all its assertions
     * @param var the variable to be deleted
     */
    public static void deleteVariable(Variable var) {
        deleteVariable(var.getSiname(),var.getName());
    }

    /**
     * Process a state change received from a remote service, e.g., "Room1.Lamp1=off"
     * @param siname service name of the variable received from a remote service in a form of "Room1"
     * @param varname variable name of the variable received from a remote service in a form of "Lamp1"
     * @param state current state of the variable
     */
    public static void changeState(String siname, String varname, String state) {
        OWLNamedIndividual ind=factory.getOWLNamedIndividual(siname+"."+varname,pm);
        // Two deletion approaches:
        // This approach avoids using reasoner
        Set<OWLDataPropertyAssertionAxiom> stateSet2=smenvOntology.getDataPropertyAssertionAxioms(ind);
        for(OWLDataPropertyAssertionAxiom ax: stateSet2) {
            if(ax.getProperty().equals(hasState)) {
                manager.removeAxiom(smenvOntology,ax);
                log.config("Property assertion of individual "+siname+"."+varname+" is deleted");
                break;
            }
        }

        // This approach uses a reasoner to find the current state
        /*reasoner=reasonerFactory.createReasoner(smenvOntology);
        Set<OWLLiteral> stateSet=reasoner.getDataPropertyValues(ind,hasState);
        reasoner.dispose();
        if(!stateSet.isEmpty()) {
            OWLLiteral oldstate=stateSet.iterator().next();
            OWLDataPropertyAssertionAxiom ax=factory.getOWLDataPropertyAssertionAxiom(hasState,ind,oldstate);
            manager.removeAxiom(smenvOntology,ax);
            log.config("Property assertion of individual "+siname+"."+varname+" is deleted");
        }*/

        OWLDataPropertyAssertionAxiom ax;
        try {
            int s=Integer.parseInt(state);
            ax=factory.getOWLDataPropertyAssertionAxiom(hasState,ind,s);
            manager.addAxiom(smenvOntology, ax);
        } catch (Exception e) {
            ax=factory.getOWLDataPropertyAssertionAxiom(hasState,ind,state);
            manager.addAxiom(smenvOntology, ax);
        } finally {
            log.config("Property assertion of individual "+siname+"."+varname+" is added");
        }
    }

    /**
     * Add assertions for the location of a new individual (environment variable), create the corresponding classes
     * first if the location is also new to the ontology
     * @param location
     */
    public static void addLocationAxioms(String location) {
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        StringTokenizer tokenizer=new StringTokenizer(location,".");
        int count=tokenizer.countTokens();
        if(count<=1) {
            classAssertion(location);
        }
        else {
            String subsumer=tokenizer.nextToken();
            OWLNamedIndividual indSubsumer=factory.getOWLNamedIndividual(subsumer,pm);
            classAssertion(subsumer);
            for(int i=2;i<=count;i++) {
                String subsumee=tokenizer.nextToken();
                OWLNamedIndividual indSubsumee=factory.getOWLNamedIndividual(subsumee,pm);
                axioms.add(factory.getOWLObjectPropertyAssertionAxiom(containsComponent,indSubsumer,indSubsumee));
                classAssertion(subsumee);
                indSubsumer=indSubsumee;
            }
            manager.addAxioms(smenvOntology,axioms);
            log.config("Object property assertions of multiple individuals "+location+" are added");
        }
    }

    /**
     * Query the super classes, equivalent classes, subclasses and instances of a given concept
     * @param query an OWL statement describing a concept
     * @param supercls boolean value indicating if super classes of the concept is requested
     * @param equivcls boolean value indicating if equivalent classes of the concept is requested
     * @param subcls boolean value indicating if sub classes of the concept is requested
     * @param ins boolean value indicating if instances of the concept is requested
     * @return answer of the query in string
     */
    public static String dlQuery(String query, boolean supercls, boolean equivcls, boolean subcls, boolean ins) {
        Set<OWLClass> classes;
        Set<OWLNamedIndividual> individuals;
        StringBuilder answer=new StringBuilder();
        answer.append("Answer of query \""+query+"\":\n\n");
        try{
            if(supercls) {
                classes=dlQueryEngine.getSuperClasses(query, false);
                printEntities("Super Classes:\n",classes,answer);
            }
            if(equivcls) {
                classes=dlQueryEngine.getEquivalentClasses(query);
                printEntities("Equivalent Classes:\n",classes,answer);
            }
            if(subcls) {
                classes=dlQueryEngine.getSubClasses(query, false);
                printEntities("Sub-Classes:\n",classes,answer);
            }
            if(ins) {
                individuals=dlQueryEngine.getInstances(query,true);
                printEntities("Individuals:\n",individuals,answer);
            }
        }
        catch (ParserException e) {
            log.warning("Unable to parse query "+query);
            return e.getMessage();
        }
        log.info("Query of "+query+" is answered");
        return answer.toString();
    }

    /**
     * Build a string of OWL entities in a tree style
     * @param title title of a group of entities
     * @param entities a set of OWL entities
     * @param answer a string builder containing the generated tree style hierarchy
     */
    public static void printEntities(String title, Set<? extends OWLEntity> entities, StringBuilder answer) {
        answer.append(title);
        if (!entities.isEmpty()) {
            for (OWLEntity entity : entities) {
                answer.append("\t");
                answer.append(shortFormProvider.getShortForm(entity));
                answer.append("\n");
            }
        } else {
            answer.append("\t[NONE]\n");
        }
        answer.append("\n");
        log.config("Entity " + title + " is printed");
    }

    /**
     * Get the classes of the environment ontology
     * @param cls an OWL class description
     * @param level the depth of hierarchy to be printed
     * @return a tree style hierarchy of classes structure
     */
    public static String printHierarchy(OWLClass cls, int level) {
        StringBuilder ontology=new StringBuilder();
        try {
            reasoner=reasonerFactory.createReasoner(smenvOntology);
            if (reasoner.isSatisfiable(cls)) {
                for (int i=0;i<level*4;i++) {
                    ontology.append(" ");
                }
                ontology.append(shortFormProvider.getShortForm(cls));
                ontology.append("\n");
                for (OWLClass child:reasoner.getSubClasses(cls, true).getFlattened()) {
                    if (!child.equals(cls)) {
                        ontology.append(printHierarchy(child, level + 1));
                    }
                }
            }
            reasoner.dispose();
        } catch (Exception e) {
            log.warning("Unable to print ontology hierarchy");
            return e.getMessage();
        }
        return ontology.toString();
    }

    /**
     * Get the properties of the environment ontology
     * @return a tree style hierarchy of properties structure
     */
    public static String printProperties() {
        StringBuilder result=new StringBuilder();
        result.append("Object Properties:\n");
        reasoner=reasonerFactory.createReasoner(smenvOntology);
        for(OWLObjectPropertyExpression ope:reasoner.getSubObjectProperties(factory.getOWLTopObjectProperty(),true).getFlattened()) {
            result.append("\t");
            result.append(shortFormProvider.getShortForm(ope.getNamedProperty()));
            result.append("\n");
        }
        result.append("\n");
        printEntities("Data Properties:\n", reasoner.getSubDataProperties(factory.getOWLTopDataProperty(), true).getFlattened(), result);
        reasoner.dispose();
        return result.toString();
    }

    /**
     * Get the individuals of the environment ontology
     * @return a list of individuals concentrated in a string
     */
    public static String printIndividuals() {
        StringBuilder result=new StringBuilder();
        reasoner=reasonerFactory.createReasoner(smenvOntology);
        printEntities("Individuals:\n", reasoner.getInstances(root, false).getFlattened(), result);
        reasoner.dispose();
        return result.toString();
    }

    /**
     * Get the individuals of the environment ontology
     * @param query an OWL concept statement
     * @return a list of individuals concentrated in a special format "ind1|ind2|ind3..."
     */
    public static String getTrimIndividuals(String query) {
        StringBuilder result=new StringBuilder();
        try {
            Set<OWLNamedIndividual> individuals=dlQueryEngine.getInstances(query,false);
            for(OWLNamedIndividual ind:individuals) {
                result.append(shortFormProvider.getShortForm(ind));
                result.append("|");
            }
        } catch (ParserException pe) {
            log.warning("Unable to collect individuals given the query");
            return pe.getMessage();
        }
        return result.toString();
    }

    /**
     * Given a rule of which the variables are described by an OWL query, generate a set of rules for all the matched
     * instances
     * @param rule a rule in a form of [PRESENCE and isContainedIn some DESK]=false=>room*.lamp?=off)
     * @return a set of generated rules divided by a "$" in a string
     */
    public static String batchRuleGeneration(String rule) {
        StringBuilder rules=new StringBuilder();
        Pattern patternNewline=Pattern.compile("\\r\\n");
        Pattern patternVar=Pattern.compile("\\[.+\\]");
        Pattern patternStar=Pattern.compile("\\*");
        Pattern patternQuestion=Pattern.compile("\\?");
        Pattern patternID=Pattern.compile("[\\d_]+");
        Matcher matcherNewline=patternNewline.matcher(rule);
        if(matcherNewline.find()) rule=matcherNewline.replaceAll("");
        Matcher matcherQuery=patternVar.matcher(rule);
        Matcher matcherID;
        Matcher matcherStar;
        Matcher matcherQuestion;
        String newrule="";
        String query="";
        String star="";
        String question="";
        if(matcherQuery.find()) {
            query=matcherQuery.group();
            query=query.substring(1,query.length()-1);
            String answer=getTrimIndividuals(query);
            log.info("Found variables "+answer+" for batch rule generation");
            if(answer.contains("Encountered")) return answer;
            StringTokenizer tokenizer=new StringTokenizer(answer,"|");
            while(tokenizer.hasMoreTokens()){
                String var=tokenizer.nextToken();
                StringTokenizer tokenizerDot=new StringTokenizer(var,".");
                if(tokenizerDot.countTokens()==2) {
                    matcherID=patternID.matcher(tokenizerDot.nextToken());
                    if(matcherID.find()) {
                        star=matcherID.group();
                    }
                    matcherID=patternID.matcher(tokenizerDot.nextToken());
                    if(matcherID.find()) {
                        question=matcherID.group();
                    }
                    newrule=matcherQuery.replaceAll(var);
                    matcherStar=patternStar.matcher(newrule);
                    if(star.length()>0&&matcherStar.find()) {
                        newrule=matcherStar.replaceAll(star);
                    }
                    matcherQuestion=patternQuestion.matcher(newrule);
                    if(question.length()>0&&matcherQuestion.find()) {
                        newrule=matcherQuestion.replaceAll(question);
                    }
                    rules.append(newrule);
                    rules.append("$");
                }
            }
            if(rules.length()==0) return "Unable to find qualified variables given the query: "+query;
            else return rules.toString();
        }
        else return "Please specify the description of ONE variable with square braces \"[\" and \"]\"" +
                " in Manchester syntax, which constrains the ranges of rules applied";
    }

    /**
     * Delete the current ontology and rebuild a new one
     * @return result (true or false)
     */
    public static String rebuildOntology() {
        try {
            ourInstance=new OntoControlApi();
            log.warning("Ontology reinitialized because of manual control or inconsistency");
            return "true";
        } catch (Exception e) {
            log.severe("Unable to reinitialize ontology");
            return e.getMessage();
        }
    }

    /**
     * Save the current ontology into a file in OWL format
     * @return result (true or false)
     */
    public static String saveOntology() {
        try {
            manager.saveOntology(smenvOntology,documentIRI);
            log.info("Ontology saved to file successfully");
            return "true";
        } catch (Exception e) {
            log.warning("Unable to save ontology");
            return e.getMessage();
        }
    }

    public static OntoControlApi getInstance() {
        return ourInstance;
    }

    public static OWLClass getRoot() {
        return root;
    }

    public static OWLReasonerFactory getReasonerFactory() {
        return reasonerFactory;
    }

    public static OWLOntology getSmenvOntology() {
        return smenvOntology;
    }
}
