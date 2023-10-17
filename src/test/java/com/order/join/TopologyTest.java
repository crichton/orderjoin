package com.order.join;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.order.join.models.MergedOrderProductModel;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

import javax.validation.Validator;
import java.util.HashMap;

import static org.apache.kafka.streams.StreamsConfig.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class TopologyTest {
    private final Serde<String> stringSerde = Serdes.String();

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
        private TestOutputTopic<String, String> sourceTopic;
        private TestOutputTopic<String, String> sourceTopicFiltered;
        private TestOutputTopic<String, String> joinTopic;
        private TestOutputTopic<String, String> errorTopic;
        private TestInputTopic<String, String> lookupTopic;
        private TestOutputTopic<String, String> lookupTopicFiltered;
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
        testContext.appConfig.setApplicationId("appid");
        testContext.appConfig.setRawTopic("rawTopic");
        testContext.appConfig.setErrorTopic("errorTopic");
        testContext.appConfig.setJoinTopic("joinTopic");
        testContext.appConfig.setLookupTopic("lookupTopic");
        testContext.appConfig.setLookupTopicFiltered("lookupTopicFiltered");
        testContext.appConfig.setSourceTopic("sourceTopic");
        testContext.appConfig.setSourceTopicFiltered("sourceTopicFiltered");
        testContext.appConfig.setCountryFilter("001");
        var topology = new TopologyBuilder(testContext.appConfig);
        StreamsBuilder builder = new StreamsBuilder();
        var transformTopology = topology.createTopology(builder);
        testContext.topologyTestDriver = new TopologyTestDriver(transformTopology , kafkaStreamsConfig().asProperties());

        // setup test topics
        testContext.rawTopic =
                testContext.topologyTestDriver.createInputTopic(
                        testContext.appConfig.getRawTopic(), stringSerde.serializer(), stringSerde.serializer());
        // setup test topics
        testContext.lookupTopic=
                testContext.topologyTestDriver.createInputTopic(
                        testContext.appConfig.getLookupTopic(), stringSerde.serializer(), stringSerde.serializer());
 
        // setup test topics
        testContext.sourceTopic=
                testContext.topologyTestDriver.createOutputTopic(
                        testContext.appConfig.getSourceTopic(), stringSerde.deserializer(), stringSerde.deserializer());

        testContext.sourceTopicFiltered=
                testContext.topologyTestDriver.createOutputTopic(
                        testContext.appConfig.getSourceTopicFiltered(), stringSerde.deserializer(), stringSerde.deserializer());

        testContext.lookupTopicFiltered=
                testContext.topologyTestDriver.createOutputTopic(
                        testContext.appConfig.getLookupTopicFiltered(), stringSerde.deserializer(), stringSerde.deserializer());

        testContext.joinTopic=
                testContext.topologyTestDriver.createOutputTopic(
                        testContext.appConfig.getJoinTopic(), stringSerde.deserializer(), stringSerde.deserializer());

        testContext.errorTopic =
                testContext.topologyTestDriver.createOutputTopic(
                        testContext.appConfig.getErrorTopic(), stringSerde.deserializer(), stringSerde.deserializer());
        return testContext;
    }

    @Test
    void test_order_deserialization_valid() {
        try (var testContext = createTestContext()) {
            var  valid = TestUtils.order_sale_valid();

            testContext.rawTopic.pipeInput("12", valid);
            assertEquals(1, testContext.sourceTopic.getQueueSize());
            String valueRaw = testContext.sourceTopic.readValue();
            assertNotNull(valueRaw);
        } catch (Exception ex) {
            Assertions.fail("Failed with error", ex);
        }
    }

    @Test
    void test_order_deserialization_invalid() {
        try (var testContext = createTestContext()) {
            var  valid = TestUtils.order_sale_invalid();

            testContext.rawTopic.pipeInput("12", valid);
            assertEquals(0, testContext.sourceTopic.getQueueSize());
            assertEquals(1, testContext.errorTopic.getQueueSize());
            String valueRaw = testContext.errorTopic.readValue();
            assertNotNull(valueRaw);
        } catch (Exception ex) {
            Assertions.fail("Failed with error", ex);
        }
    }

    @Test
    void test_product_deserialization_valid() {
        try (var testContext = createTestContext()) {
            var  valid = TestUtils.product_valid();
            testContext.lookupTopic.pipeInput("13", valid);
            assertEquals(1, testContext.lookupTopicFiltered.getQueueSize());
            assertEquals(0, testContext.errorTopic.getQueueSize());
            String valueRaw = testContext.lookupTopicFiltered.readValue();
            assertNotNull(valueRaw);
        } catch (Exception ex) {
            Assertions.fail("Failed with error", ex);
        }
    }


    @Test
    void test_source_topic_filtered() {
        try (var testContext = createTestContext()) {
            var  valid = TestUtils.order_sale_valid();
            testContext.rawTopic.pipeInput("12", valid);
            assertEquals(1, testContext.sourceTopic.getQueueSize());
            String valueRaw = testContext.sourceTopic.readValue();
            assertNotNull(valueRaw);
            assertEquals(0, testContext.errorTopic.getQueueSize());
            assertEquals(1, testContext.sourceTopicFiltered.getQueueSize());
        } catch (Exception ex) {
            Assertions.fail("Failed with error", ex);
        }
    }


    @Test
    void test_join_topic() {
        try (var testContext = createTestContext()) {
           var  pvalid = TestUtils.product_valid();
            testContext.lookupTopic.pipeInput("13", pvalid);
            var  ovalid = TestUtils.order_sale_valid();
            testContext.rawTopic.pipeInput("12", ovalid);
            // check join worked
            assertEquals(1, testContext.joinTopic.getQueueSize());
            String valueRaw = testContext.joinTopic.readValue();
            assertNotNull(valueRaw);
            // check no errors
            assertEquals(0, testContext.errorTopic.getQueueSize());
            // check desrialization and validation of merged object
            MergedOrderProductModel mergedModel = TestUtils.jsonMapper().readValue(valueRaw, MergedOrderProductModel.class);
            assertNotNull(mergedModel);
            assertTrue(DataValidator.validate(mergedModel));
            JSONAssert.assertEquals(TestUtils.merged_order_product_valid(), valueRaw, JSONCompareMode.LENIENT);
        } catch (Exception ex) {
            Assertions.fail("Failed with error", ex);
        }
    }
}