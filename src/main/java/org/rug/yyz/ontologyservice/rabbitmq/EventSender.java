package org.rug.yyz.ontologyservice.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Created with IntelliJ IDEA.
 * User: yuanzhe
 * Date: 13-12-9
 * Time: 下午5:37
 * To change this template use File | Settings | File Templates.
 */

/**
 * The class to send messages to other remote services through RabbitMQ
 */
public class EventSender {
    private static EventSender ourInstance = new EventSender();
    private static Channel channel;

    public static EventSender getInstance() {
        return ourInstance;
    }

    /**
     * Establish connection to a RabbitMQ server
     */
    public static void establishConnection() {
        try {
            ConnectionFactory factory=new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection= factory.newConnection();
            channel=connection.createChannel();
            channel.queueDeclare("Event", false, false, false, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Send a message
     * @param msg the content of message
     */
    public static void sendMessage(String msg) {
        try {
            channel.basicPublish("", "Event", null, msg.getBytes());
        }  catch (Exception e) {
            e.printStackTrace();
        }
    }
}
