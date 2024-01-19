package org.mae;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class Producer {
    public static void main(String[] args) {

        Runnable kafkaTask = () -> {


            Properties props = new Properties();
            props.put("bootstrap.servers", "localhost:9092");
            props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

            KafkaProducer<String, String> producer = new KafkaProducer<>(props);

            String topic = "my-topic";
            String message = "Hello, Kafka!";

            ProducerRecord<String, String> record = new ProducerRecord<>(topic, message);

            producer.send(record);

            producer.close();
        };

        Runnable rabbitTask = () -> {
            String QUEUE_NAME = "my-queue";

            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            String message = "Hello, RabbitMQ!";
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
            System.out.println("Sent message: " + message);

            channel.close();
            connection.close();} catch (IOException | TimeoutException e){
                e.printStackTrace();
            }
        };
    }
}