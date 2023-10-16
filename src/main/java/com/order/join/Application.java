package com.order.join;

import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@SpringBootApplication
@EnableKafkaStreams
public class Application {

    TopologyBuilder data = new TopologyBuilder();

    @Autowired 
    private ApplicationConfiguration appConfig;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    public ApplicationConfiguration config() {
        return appConfig;
    }

    // Kafka Streams configuration
    @Bean
    public KafkaStreams kafkaStreams() {
        Properties properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, appConfig.getApplicationId());
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, appConfig.getBootstrapServers()); // Kafka broker
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, appConfig.getAutoOffsetReset());
        Topology joinTopology = TopologyBuilder.createTopology(appConfig);
        // Create and start the KafkaStreams instance
        KafkaStreams kafkaStreams = new KafkaStreams(joinTopology, properties);
        kafkaStreams.start();

        // Shutdown hook to gracefully close KafkaStreams on application shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));
        return kafkaStreams;
    }
   
}