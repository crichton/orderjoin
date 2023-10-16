package com.order.join;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.Serdes;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.order.join.ApplicationConfiguration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
//@ContextConfiguration(classes= {AppProperties.class, KafkaProperties.class})
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
@ActiveProfiles("test")
@EnableKafka
class StandardOrderIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    EmbeddedKafkaBroker kafkaEmbedded;

    @Autowired
    ApplicationConfiguration appProperties;

    //@Test
    void testCallApiAndPublishToKafka() throws Exception {
        /*
        String expectedResponse = "GB1000002000ST";
        WebClient.ResponseSpec responseSpec = Mockito.mock(WebClient.ResponseSpec.class);
        Mono mono = Mockito.mock(Mono.class);
        when(mono.block(any())).thenReturn(expectedResponse);
        when(responseSpec.bodyToMono(String.class)).thenReturn(mono);
        RequestHeadersUriSpec requestHeadersUriSpec = Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersUriSpec);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
 
        Consumer<String, String> consumer = configureConsumer();
        var response1 = mockMvc
                .perform(post("/it-order-enrichment-service/12345678/orders").contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.mockStandardOrder()))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        System.out.format("response:%s%n", response1);
        // assertThat(appProperties.getOrderIdPrefix()).isNotEmpty();
        ObjectMapper jsonmapper = new JsonMapper();
        jsonmapper.registerModule(new JsonNullableModule());
        jsonmapper.registerModule((new JavaTimeModule()));
        OrderResponse orderResponse = jsonmapper.readValue(response1, OrderResponse.class);
        ConsumerRecord<String, String> singleRecord = KafkaTestUtils.getSingleRecord(consumer,
                kafkaProperties.getTargetTopic());
        assertThat(singleRecord).isNotNull();
        assertThat(singleRecord.key()).isEqualTo(expectedResponse);
        Order order = jsonmapper.readValue(singleRecord.value(), Order.class);
        assertThat(order).isNotNull();
        assertThat(order.getOrderNumber().get()).isEqualTo(expectedResponse);
        JSONAssert.assertEquals(TestUtils.mockStandardOrderEvent70(), singleRecord.value(),
                    new CustomComparator(JSONCompareMode.STRICT,
                            new Customization("header.eventDate", (o1, o2) -> true)));
        
        */
    }

    private Consumer<String, String> configureConsumer() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", kafkaEmbedded);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<String, String>(consumerProps,
                new Serdes.StringSerde().deserializer(), new Serdes.StringSerde().deserializer()).createConsumer();
        consumer.subscribe(Collections.singleton(appProperties.getRawTopic()));
        return consumer;
    }

}
