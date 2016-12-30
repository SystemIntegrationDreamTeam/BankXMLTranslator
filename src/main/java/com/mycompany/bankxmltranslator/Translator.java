/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.bankxmltranslator;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 *
 * @author Buhrkall
 */
public class Translator {

    static final String SENDING_QUEUE_NAME = "cphbusiness.bankXML";
    static final String LISTENING_QUEUE_NAME = "BankXMLTranslatorQueue";
    static final String REPLY_TO_HEADER = "NormalizerQueue";

    final static AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
            .replyTo(REPLY_TO_HEADER)
            .build();

    static String message = "";

    public static void main(String[] args) throws IOException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("datdb.cphbusiness.dk");
        factory.setVirtualHost("student");
        factory.setUsername("Dreamteam");
        factory.setPassword("bastian");
        Connection connection = factory.newConnection();
        final Channel listeningChannel = connection.createChannel();
        final Channel sendingChannel = connection.createChannel();

        listeningChannel.queueDeclare(LISTENING_QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        Consumer consumer = new DefaultConsumer(listeningChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                try {
                    message = new String(body, "UTF-8");
                    System.out.println(" [x] Received '" + message + "'");

                    String[] arr = message.split(",");

                    Result res = new Result(arr[0], Integer.parseInt(arr[1]), Double.parseDouble(arr[2]), Integer.parseInt(arr[3]));

                    JAXBContext jaxbContext = JAXBContext.newInstance(Result.class);
                    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

                    StringWriter sw = new StringWriter();
                    jaxbMarshaller.marshal(res, sw);
                    String xmlString = sw.toString();

                    System.out.println(xmlString);

              // HANDLE MESSAGE HERE
                    
                    sendingChannel.exchangeDeclare(SENDING_QUEUE_NAME, "fanout");
                    String test = sendingChannel.queueDeclare().getQueue();
                    sendingChannel.queueBind(test,SENDING_QUEUE_NAME,"");
                    sendingChannel.basicPublish("", SENDING_QUEUE_NAME, props, xmlString.getBytes());

                } catch (JAXBException ex) {
                    Logger.getLogger(Translator.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        };
        listeningChannel.basicConsume(LISTENING_QUEUE_NAME, true, consumer);

    }

}
