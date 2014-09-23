package org.rug.yyz.ontologyservice.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.rug.yyz.ontologyservice.ontology.OntoControlApi;
import org.rug.yyz.ontologyservice.ontology.Variable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: yuanzhe
 * Date: 13-12-2
 * Time: 下午1:32
 * To change this template use File | Settings | File Templates.
 */

/**
 * The class to handle the messages received from other remote services
 */
public class EventReceiver {
    private static EventReceiver ourInstance = new EventReceiver();
    private static Logger log;

    public static EventReceiver getInstance() {
        return ourInstance;
    }

    private EventReceiver() {
        log=Logger.getLogger("RabbitMQLogs");
    }

    /**
     * Establish connection to a RabbitMQ server and prepare to receive incoming messages
     */
    public static void establishConnection() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclare("Event", false, false, false, null);
            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume("Event", true, consumer);
            QueueingConsumer.Delivery delivery;
            String message;
            log.info("Connection to RabbitMQ Server established");
            while (true) {
                delivery = consumer.nextDelivery();
                message = new String(delivery.getBody());
                // convert a state change to assertions for the ontology, if the ontology becomes inconsistent, rebuild
                // a new one
                try {
                    processEvent(message);
                } catch (InconsistentOntologyException ioe) {
                    if(OntoControlApi.rebuildOntology().equals("true")) {
                        log.warning("Ontology reinitialized, reprocess message: "+message);
                        processEvent(message);
                    }
                }
            }
        } catch (IOException ioe) {
            log.severe("Unable to connect to RabbitMQ Server");
        } catch (InterruptedException ie) {
            log.severe("Connection to RabbitMQ Server is interrupted");
        }
    }

    /**
     * For each incoming message from a remote service, process the request depending on the size of the message
     * since all messages have a fix format
     * @param message e.g., a state change has a form of "Room1|Lamp1|off"
     */
    public static void processEvent(String message) {
        StringTokenizer tokenizer=new StringTokenizer(message,"|");
        final int NEW_VARIABLE = 7;
        final int STATE_CHANGE = 3;
        final int VARAIBLE_DELETION = 2;
        final int PERFORMANCE_TEST = 1;
        List<String> list=new ArrayList<String>();
        int size=tokenizer.countTokens();
        switch (size) {
            case NEW_VARIABLE:
                while(tokenizer.hasMoreTokens()){
                    list.add(tokenizer.nextToken());
                }
                Variable var=tokensToVariable(list);
                OntoControlApi.addVariable(var);
                break;
            case STATE_CHANGE:
                OntoControlApi.changeState(tokenizer.nextToken(),tokenizer.nextToken(),tokenizer.nextToken());
                break;
            case VARAIBLE_DELETION:
                OntoControlApi.deleteVariable(tokenizer.nextToken(),tokenizer.nextToken());
                break;
            case PERFORMANCE_TEST:
                Runtime r = Runtime.getRuntime();
                long t1 = r.totalMemory()/1024;
                long f1 = r.freeMemory()/1024;
                System.out.println(t1+"KB");
                System.out.println(f1+"KB");
                System.out.println("Test finished at "+System.currentTimeMillis());
                break;
        }
        log.info("Processed message: "+message);
    }

    /**
     * Create a Variable instance given a list of attributes of an environment variable
     * @param list a list of attributes of an environment variable received from a remote service
     * @return a Variable instance
     */
    private static Variable tokensToVariable(List<String> list) {
        int id=Integer.parseInt(list.get(0));
        String siname=list.get(1);
        String name=list.get(2);
        String location=list.get(3);
        boolean controllable=Boolean.parseBoolean(list.get(4));
        String domain=list.get(5);
        boolean slow=Boolean.parseBoolean(list.get(6));
        return new Variable(id,siname,name,location,controllable,domain,slow);
    }

}
