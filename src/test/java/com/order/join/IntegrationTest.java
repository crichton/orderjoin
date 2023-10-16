package com.order.join;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = { "us.order.raw", "us.order.raw.error", "us.order.canonical","us.order.001.filtered","us.order.product.001.joined", "us.product.canonical","us.product.001.canonical"})
@EnableKafka
@DirtiesContext
@ActiveProfiles("test")
public class IntegrationTest {

    private static final String INPUT_TOPIC = "inputTopic";
    private static final String OUTPUT_TOPIC = "outputTopic";

    @Autowired
    private KafkaListenerEndpointRegistry endpointRegistry;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;
    @Autowired
    ApplicationConfiguration appConfig;

    private Producer<String, String> producer;
    private Consumer<String, String> consumer;

    @BeforeEach
    public void setUp() {
        // Configure the producer properties
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka.getBrokersAsString());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Create the producer
        ProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);
        producer = producerFactory.createProducer();

        // Configure the consumer properties
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-group", "false", embeddedKafka.getBrokersAsString());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        // Create the consumer
        ConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        consumer = consumerFactory.createConsumer();
        consumer.subscribe(Collections.singleton(appConfig.getJoinTopic()));

        // Wait for the consumer to be ready
        ContainerTestUtils.waitForAssignment(consumer, embeddedKafka.getPartitionsPerTopic());
    }

    @AfterEach
    public void tearDown() {
        producer.close();
        consumer.close();
    }

    @Test
    public void testKafkaIntegration() throws InterruptedException {
        // Produce a message to the input topic
        producer.send(new ProducerRecord<>(appConfig.getRawTopic(), "key", "value"));

        // Wait for the message to be processed by the Kafka listener
        ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, appConfig.getJoinTopic());
        assertEquals("value", record.value());
    }

    // Define a Kafka listener
    @KafkaListener(topics = INPUT_TOPIC, groupId = "test-group")
    public void listen(String message) {
        // Process the message and produce the result to the output topic
        String result = message.toUpperCase();
        producer.send(new ProducerRecord<>(OUTPUT_TOPIC, "key", result));
    }
}
