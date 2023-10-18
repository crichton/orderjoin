package com.order.join;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@EnableKafkaStreams
@EnableKafka
@ConfigurationProperties(prefix = "orderjoin")
public class ApplicationConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfiguration.class);

    private String trustedPackages;
    private String applicationId;
    private String bootstrapServers;
    private String autoOffsetReset;
    private String rawTopic;
    private String sourceTopic;
    private String sourceTopicFiltered;
    private String lookupTopic;
    private String lookupTopicFiltered;
    private String joinTopic;
    private String errorTopic;
    private String countryFilter;

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper jsonmapper = new JsonMapper();
        jsonmapper.registerModule(new JsonNullableModule());
        jsonmapper.registerModule((new JavaTimeModule()));
        jsonmapper.setDateFormat(new StdDateFormat());
        return jsonmapper;
    }

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    KafkaStreamsConfiguration kStreamsConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, getApplicationId());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers()); // Kafka broker
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,getAutoOffsetReset());
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        return new KafkaStreamsConfiguration(props);
    }

    public String getTrustedPackages() {
        return trustedPackages;
    }

    public void setTrustedPackages(String trustedPackages) {
        this.trustedPackages = trustedPackages;
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }
    public String getRawTopic() {
        return rawTopic;
    }

    public void setRawTopic(String rawTopic) {
        this.rawTopic = rawTopic;
    }
    

    public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public String getAutoOffsetReset() {
		return autoOffsetReset;
	}
	public void setAutoOffsetReset(String autoOffsetReset) {
		this.autoOffsetReset = autoOffsetReset;
	}
	public String getSourceTopic() {
        return sourceTopic;
    }
    public void setSourceTopic(String sourceTopic) {
        this.sourceTopic = sourceTopic;
    }
    public String getLookupTopic() {
        return lookupTopic;
    }
    public void setLookupTopic(String lookupTopic) {
        this.lookupTopic = lookupTopic;
    }
    public String getJoinTopic() {
        return joinTopic;
    }
    public void setJoinTopic(String joinTopic) {
        this.joinTopic = joinTopic;
    }
    public String getErrorTopic() {
        return errorTopic;
    }
    public void setErrorTopic(String errorTopic) {
        this.errorTopic = errorTopic;
    }
    public String getCountryFilter() {
        return countryFilter;
    }
    public void setCountryFilter(String countryFilter) {
        this.countryFilter = countryFilter;
    }
    public String getSourceTopicFiltered() {
        return sourceTopicFiltered;
    }
    public void setSourceTopicFiltered(String sourceTopicFiltered) {
        this.sourceTopicFiltered = sourceTopicFiltered;
    }
    public String getLookupTopicFiltered() {
        return lookupTopicFiltered;
    }
    public void setLookupTopicFiltered(String lookupTopicFiltered) {
        this.lookupTopicFiltered = lookupTopicFiltered;
    }
    @Override
    public String toString() {
        return "AppProperties{" + "RawTopic='" + rawTopic+ '\'' + '}';
    }
}