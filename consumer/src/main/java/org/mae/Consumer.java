package org.mae;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Collections;
import java.util.Properties;

public class Consumer {
    public static void main(String[] args) {


        Runnable kafkaTask = () -> {
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            props.put(ConsumerConfig.GROUP_ID_CONFIG, "my-group");
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

            String topic = "my-topic";

            consumer.subscribe(Collections.singletonList(topic));

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(1000);
                records.forEach(record -> {
                    System.out.println("Received message: " + record.value());
                });
            }
        };
        Runnable rabbitTask = () -> {
            String QUEUE_NAME = "my-queue";

            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            try {
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();

                channel.queueDeclare(QUEUE_NAME, false, false, false, null);

                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), "UTF-8");
                    System.out.println("Received message: " + message);
                };

                channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
                });

                System.out.println("Waiting for messages...");
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
        };
        Thread kafka = new Thread(kafkaTask);
        kafka.start();

        Thread rabbit = new Thread(rabbitTask);
        rabbit.start();
    }
}