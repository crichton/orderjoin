package com.order.join;

import java.util.Properties;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;

public class TopologyBuilder {

    public TopologyBuilder() {
    }

    public static org.apache.kafka.streams.Topology createTopology(ApplicationConfiguration appConfig) {
        StreamsBuilder builder = new StreamsBuilder();

        // Create a KStream from the input topic
        KStream<String, String> inputTopicStream = builder.stream(appConfig.getRawTopic(),
                Consumed.with(Serdes.String(), Serdes.String()));

        // Perform validation or processing on the data (modify as per your validation
        // logic)
        KStream<String, String> validatedStream = inputTopicStream.filter((key, value) -> isValid(value));

        // Process the validated data (you can add more processing logic here)

        // Shutdown hook to gracefully close KafkaStreams on application shutdown
        return builder.build();
    }

    private static Boolean isValid(String value) {
        // TODO Auto-generated method stub
        return null;
    }
}