package org.rug.yyz.ontologyservice.ontology;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.ShortFormProvider;

import java.util.Collections;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: yuanzhe
 * Date: 13-12-2
 * Time: 下午9:20
 * To change this template use File | Settings | File Templates.
 */

/**
 * The class of a DL query engine
 */
public class DLQueryEngine {
    private final DLQueryParser parser;
    private static OWLReasoner reasoner;

    public DLQueryEngine(ShortFormProvider shortFormProvider) {
        parser = new DLQueryParser(OntoControlApi.getSmenvOntology(), shortFormProvider);
    }

    /**
     * Get super classes of a concept described by a OWL class expression
     * @param classExpressionString a OWL class expression
     * @param direct indicates direct parent or ancestors
     * @return a set of OWL classes
     */
    public Set<OWLClass> getSuperClasses(String classExpressionString, boolean direct) {
        if (classExpressionString.trim().length() == 0) {
            return Collections.emptySet();
        }
        reasoner=OntoControlApi.getReasonerFactory().createReasoner(OntoControlApi.getSmenvOntology());
        OWLClassExpression classExpression = parser
                .parseClassExpression(classExpressionString);
        NodeSet<OWLClass> superClasses = reasoner
                .getSuperClasses(classExpression, direct);
        reasoner.dispose();
        return superClasses.getFlattened();
    }

    /**
     * Get the equivalent classes of a concept described by a OWL class expression
     * @param classExpressionString a OWL class expression
     * @return a set of OWL classes
     */
    public Set<OWLClass> getEquivalentClasses(String classExpressionString) {
        if (classExpressionString.trim().length() == 0) {
            return Collections.emptySet();
        }
        reasoner=OntoControlApi.getReasonerFactory().createReasoner(OntoControlApi.getSmenvOntology());
        OWLClassExpression classExpression = parser
                .parseClassExpression(classExpressionString);
        Node<OWLClass> equivalentClasses = reasoner.getEquivalentClasses(classExpression);
        Set<OWLClass> result;
        if (classExpression.isAnonymous()) {
            result = equivalentClasses.getEntities();
        } else {
            result = equivalentClasses.getEntitiesMinus(classExpression.asOWLClass());
        }
        reasoner.dispose();
        return result;
    }

    /**
     * Get super classes of a concept described by a OWL class expression
     * @param classExpressionString a OWL class expression
     * @param direct indicates direct children or decedents
     * @return a set of OWL classes
     */
    public Set<OWLClass> getSubClasses(String classExpressionString, boolean direct) {
        if (classExpressionString.trim().length() == 0) {
            return Collections.emptySet();
        }
        reasoner=OntoControlApi.getReasonerFactory().createReasoner(OntoControlApi.getSmenvOntology());
        OWLClassExpression classExpression = parser
                .parseClassExpression(classExpressionString);
        NodeSet<OWLClass> subClasses = reasoner.getSubClasses(classExpression, direct);
        reasoner.dispose();
        return subClasses.getFlattened();
    }

    /**
     * Get individuals of a class described by a OWL class expression
     * @param classExpressionString a OWL class expression
     * @param direct indicates direct children or decedents
     * @return a set of OWL classes
     */
    public Set<OWLNamedIndividual> getInstances(String classExpressionString,
                                                boolean direct) {
        if (classExpressionString.trim().length() == 0) {
            return Collections.emptySet();
        }
        reasoner=OntoControlApi.getReasonerFactory().createReasoner(OntoControlApi.getSmenvOntology());
        OWLClassExpression classExpression = parser
                .parseClassExpression(classExpressionString);
        NodeSet<OWLNamedIndividual> individuals = reasoner.getInstances(classExpression,
                direct);
        reasoner.dispose();
        return individuals.getFlattened();
    }
}
