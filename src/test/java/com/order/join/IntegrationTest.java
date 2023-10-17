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
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = { "us.order.raw", "us.order.raw.error", "us.order.canonical","us.order.001.filtered","us.order.product.001.joined", "us.product.canonical","us.product.001.filtered"})
@EnableKafka
@DirtiesContext
@ActiveProfiles("test")
public class IntegrationTest {


    @Autowired
    private KafkaListenerEndpointRegistry endpointRegistry;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;
    @Autowired
    ApplicationConfiguration appConfig;

    private Producer<String, String> producer;
    private Consumer<String, String> consumer;
    private Consumer<String, String> consumerError;

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
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(embeddedKafka.getBrokersAsString(), "test-group", "false");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        // Create the consumer
        ConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        consumer = consumerFactory.createConsumer();
        consumer.subscribe(Collections.singleton(appConfig.getJoinTopic()));
        consumerError = consumerFactory.createConsumer();
        consumerError.subscribe(Collections.singleton(appConfig.getErrorTopic()));


    }

    @AfterEach
    public void tearDown() {
        producer.close();
        consumer.close();
        consumerError.close();
    }

    @Test
    public void testKafkaIntegration() throws InterruptedException, IOException {
        // Produce a message to the input topic
        producer.send(new ProducerRecord<>(appConfig.getLookupTopic(), "key", TestUtils.product_valid()));
        producer.send(new ProducerRecord<>(appConfig.getRawTopic(), "key", TestUtils.order_sale_valid()));
        ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, appConfig.getJoinTopic());
        JSONAssert.assertEquals(TestUtils.merged_order_product_valid(), record.value(), JSONCompareMode.LENIENT);
    }

    @Test
    public void testKafkaIntegrationError() throws InterruptedException, IOException {
        // Produce a message to the input topic that has an error in the format
        producer.send(new ProducerRecord<>(appConfig.getLookupTopic(), "key", TestUtils.product_valid()));
        producer.send(new ProducerRecord<>(appConfig.getRawTopic(), "key", TestUtils.order_sale_invalid()));
        // goes to the error topic
        ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumerError, appConfig.getErrorTopic());
        JSONAssert.assertEquals(TestUtils.order_sale_invalid(), record.value(), JSONCompareMode.LENIENT);
    }

}
