package com.order.join;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

import javax.validation.Validator;
import java.util.HashMap;

import static org.apache.kafka.streams.StreamsConfig.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class TopologyTest {
    private final Serde<String> stringSerde = Serdes.String();

    private static final String RAW_TOPIC = "source.topic";
    private static final String TARGET_TOPIC = "canonical.topic";
    private static final String ERROR_TOPIC = "error.topic";

    private KafkaStreamsConfiguration kafkaStreamsConfig() {
        final var streamsConfig = new HashMap<String, Object>();
        streamsConfig.put(BOOTSTRAP_SERVERS_CONFIG, "bootstrap-server:9092");
        streamsConfig.put(APPLICATION_ID_CONFIG, "testApp");
        streamsConfig.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfig.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        return new KafkaStreamsConfiguration(streamsConfig);
    }

    private static class TestContext implements AutoCloseable {
        private TestInputTopic<String, String> rawTopic;
        private TestOutputTopic<String, String> targetTopic;
        private TestOutputTopic<String, String> errorTopic;
        private TopologyTestDriver topologyTestDriver;
        private ApplicationConfiguration appConfig;

        @Override
        public void close() {
            this.topologyTestDriver.close();
        }
    }

    private TestContext createTestContext() {
        var testContext = new TestContext();
        testContext.appConfig = new ApplicationConfiguration();
        testContext.appConfig.setRawTopic(RAW_TOPIC);
        var om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        om.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        var transformTopology = TopologyBuilder.createTopology(testContext.appConfig);
        testContext.topologyTestDriver = new TopologyTestDriver(transformTopology , kafkaStreamsConfig().asProperties());

        // setup test topics
        testContext.rawTopic =
                testContext.topologyTestDriver.createInputTopic(
                        testContext.appConfig.getRawTopic(), stringSerde.serializer(), stringSerde.serializer());
/*        testContext.targetTopic =
                testContext.topologyTestDriver.createOutputTopic(
                        testContext.topicsConfig.getTargetTopic(), stringSerde.deserializer(), stringSerde.deserializer());
        testContext.errorTopic =
                testContext.topologyTestDriver.createOutputTopic(
                        testContext.topicsConfig.getErrorTopic(), stringSerde.deserializer(), stringSerde.deserializer());
                        */
        return testContext;
    }

    @Test
    void test_deserialization() {
        try (var testContext = createTestContext()) {
            /*var poJson_1 = contentOf("inventory-valid.json", getClass().getClassLoader());

            testContext.rawTopic.pipeInput("12", poJson_1);

            assertEquals(1, testContext.targetTopic.getQueueSize());
            testContext.targetTopic.readValue();
            */
        } catch (Exception ex) {
            Assertions.fail("Failed with error", ex);
        }
    }
}