/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.bankxmltranslator;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
    static final String EXCHANGE_NAME = "TranslatorExchange";
    static final String REPLY_TO_HEADER = "NormalizerQueue";

    final static AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
            .replyTo(REPLY_TO_HEADER)
            .build();

    static String message = "";

    public static void main(String[] args) throws IOException {

        ConnectionFactory listeningFactory = new ConnectionFactory();
        listeningFactory.setHost("datdb.cphbusiness.dk");
        listeningFactory.setUsername("Dreamteam");
        listeningFactory.setPassword("bastian");
        
        ConnectionFactory sendingFactory = new ConnectionFactory();
        sendingFactory.setHost("datdb.cphbusiness.dk");
        sendingFactory.setUsername("Dreamteam");
        sendingFactory.setPassword("bastian");
        
        
        Connection listeningConnection = listeningFactory.newConnection();
        Connection sendingConnection = sendingFactory.newConnection();
        
        final Channel listeningChannel = listeningConnection.createChannel();
        final Channel sendingChannel = sendingConnection.createChannel();

       
         listeningChannel.queueDeclare(LISTENING_QUEUE_NAME, false, false, false, null);
         listeningChannel.queueBind(LISTENING_QUEUE_NAME, EXCHANGE_NAME, "CphBusinessXML");
        
        
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        Consumer consumer = new DefaultConsumer(listeningChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                try {
                    message = new String(body, "UTF-8");
                    System.out.println(" [x] Received '" + message + "'");

                    String[] arr = message.split(",");

//                    String ssnFix = arr[0].substring(2);
                    String dashRemoved = arr[0].replace("-", "");
                    
                    Result res = new Result(dashRemoved, Integer.parseInt(arr[1]), Double.parseDouble(arr[2]), theDateAdder(Integer.parseInt(arr[3])));

                    JAXBContext jaxbContext = JAXBContext.newInstance(Result.class);
                    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

                    StringWriter sw = new StringWriter();
                    jaxbMarshaller.marshal(res, sw);
                    String xmlString = sw.toString();
                    
                    xmlString = xmlString.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>", "");

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
    
    public static String theDateAdder(int days) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.setTime(new Date()); // Now use today date
        c.add(Calendar.DATE, days); // Adding 5 days
        String output = sdf.format(c.getTime()) + " 01:00:00.0 CET";
        return output;
    };

}
