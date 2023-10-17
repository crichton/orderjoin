package com.order.join;

import java.util.Map;
import java.util.Properties;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.order.join.models.Audit;
import com.order.join.models.Audit.AuditBuilder;
import com.order.join.models.Key;
import com.order.join.models.Key.KeyBuilder;
import com.order.join.models.MergedOrderProductModel;
import com.order.join.models.MergedOrderProductModel.MergedOrderProductModelBuilder;
import com.order.join.models.OrderSaleModel;
import com.order.join.models.ProductRegistrationModel;
import com.order.join.models.Value;
import com.order.join.models.Value.ValueBuilder;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Branched;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Joined;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Named;

@Component
public class TopologyBuilder {
    private static final Logger log = LoggerFactory.getLogger(TopologyBuilder .class);

    ApplicationConfiguration appConfig;

    @Autowired
    public TopologyBuilder(ApplicationConfiguration appConfig) {
        Objects.requireNonNull(appConfig, "appConfig is null");
        this.appConfig = appConfig;
    }

    @Autowired
    public Topology createTopology(StreamsBuilder builder) {
        // Create a KStream from the raw input topic
        KStream<String, String> inputTopicStream = builder.stream(appConfig.getRawTopic(),
                Consumed.with(Serdes.String(), Serdes.String()));

        // Data Governance: put valid value in canonical/source topic and invalid values in error topic
        Map<String, KStream<String,String>> validorInvalidStreams = inputTopicStream.split(Named.as("Branch-"))
                .branch((key, value) -> isValid(key, value),
                        Branched.as("Valid")) 
                .defaultBranch(Branched.as("Invalid"));
        KStream<String, String> valid = validorInvalidStreams.get("Branch-Valid");
        KStream<String, String> invalid = validorInvalidStreams.get("Branch-Invalid");
        valid.to(appConfig.getSourceTopic());
        invalid.to(appConfig.getErrorTopic());

        // open new source topic stream containing canonical Orders
        KStream<String, String> soruceTopicStream = builder.stream(appConfig.getSourceTopic(),
                Consumed.with(Serdes.String(), Serdes.String()));

        // filter canonical/source topic
        // filter by country
        soruceTopicStream.filter((k,v)-> filterOrderByCountry(k,v, appConfig.getCountryFilter()))
        // map the key into the canonical/source filtered topic
             .map((k,v)-> KeyValue.pair(mapOrderKey(k, v), mapOrderValue(k, v)))
             .to(appConfig.getSourceTopicFiltered());

        KStream<String, String> lookupTopicStream = builder.stream(appConfig.getLookupTopic(),
                Consumed.with(Serdes.String(), Serdes.String()));

        // filter lookup topic 
        lookupTopicStream.filter((k,v)-> filterProductByCountry(k,v, appConfig.getCountryFilter()))
        // key the filter lookup topic 
             .map((k,v)-> KeyValue.pair(mapProductKey(k, v), mapProductValue(k, v)))
             .to(appConfig.getLookupTopicFiltered());

        KStream<String, String> filteredSourceStream = builder.stream(appConfig.getSourceTopicFiltered(),
                Consumed.with(Serdes.String(), Serdes.String()));

        KTable<String, String> filteredLookupTable = builder.table(appConfig.getLookupTopicFiltered(),
                Consumed.with(Serdes.String(), Serdes.String()));
        filteredSourceStream.join(filteredLookupTable, (lv, rv) -> mergeOrderProduct(lv, rv), 
                                                        Joined.keySerde(Serdes.String())
                                                              .valueSerde(Serdes.String()))
        .to(appConfig.getJoinTopic());;
        return builder.build();
    }

    private OrderSaleModel readOrder(String order) throws JsonMappingException, JsonProcessingException {
        ObjectMapper om = appConfig.objectMapper();
        OrderSaleModel osm = om.readValue(order, OrderSaleModel.class);
        return osm;
    }

    private ProductRegistrationModel readProduct(String product) throws JsonMappingException, JsonProcessingException {
        ObjectMapper om = appConfig.objectMapper();
        ProductRegistrationModel prm = om.readValue(product, ProductRegistrationModel.class);
        return prm;
    }

    private boolean isValid(String key, String value) {
        boolean result = false;
        try {
            result = DataValidator.validate(readOrder(value));
        } catch (Throwable t) {
            return false;
        }
        return result;
    }

    private boolean filterOrderByCountry(String key, String value, String countryFilter) {
        try {
            OrderSaleModel osm = readOrder(value);
            if (osm.getKey().getCountry().equals(countryFilter)) {
                return true;
            }
        } catch (JsonProcessingException e) {
            log.error(Throwables.getStackTraceAsString(e));
            throw new RuntimeException(e);
        }
        return false;
    }

    private boolean filterProductByCountry(String key, String value, String countryFilter) {
        try {
            ProductRegistrationModel prm = readProduct(value);
            if (prm.getKey().getCountry().equals(countryFilter)) {
                return true;
            }
        } catch (Exception e) {
            log.error(Throwables.getStackTraceAsString(e));
            throw new RuntimeException(e);
        }
        return false;

    }

    private String mapOrderKey(String key, String value ) {
        try {
            OrderSaleModel osm = readOrder(value);
            return osm.getValue().getCatalogNumber();
        } catch (JsonProcessingException e) {
            log.error(Throwables.getStackTraceAsString(e));
            throw new RuntimeException(e);
        }
    }

    private String mapOrderValue(String key, String value) {
        return value;
    }

    private String mapProductKey(String key, String value ) {
        try {
            ProductRegistrationModel prm = readProduct(value);
            return prm.getValue().getCatalogNumber();
        } catch (JsonProcessingException e) {
            log.error(Throwables.getStackTraceAsString(e));
            throw new RuntimeException(e);
        }
    }

    private String mapProductValue(String key, String value) {
        return value;
    }

    private String mergeOrderProduct(String order, String product) {
        try {
            OrderSaleModel osm = readOrder(order);
            ProductRegistrationModel prm = readProduct(product);

            KeyBuilder kb =  new KeyBuilder();
            Key key = kb.withCountry(osm.getKey().getCountry())
              .withCatalogNumber(osm.getValue().getCatalogNumber())
              .build();
            
            AuditBuilder ab =  new AuditBuilder();
            Audit audit = ab.withEventName(osm.getAudit().getEventName() + "+" + prm.getAudit().getEventName())
              .withSourceSystem(osm.getAudit().getSourceSystem() + "+" + prm.getAudit().getSourceSystem())
              .build();

            ValueBuilder vb = new ValueBuilder();
            Value value = vb.withCatalogNumber(osm.getValue().getCatalogNumber())
              .withCountry(osm.getValue().getCountry())
              .withIsSelling(prm.getValue().getIsSelling())
              .withModel(prm.getValue().getModel())
              .withOrderNumber(osm.getValue().getOrderNumber())
              .withProductId(prm.getValue().getProductId())
              .withQuantity(osm.getValue().getQuantity())
              .withRegistrationId(prm.getValue().getRegistrationId())
              .withRegistrationNumber(prm.getValue().getRegistrationNumber())
              .withSalesDate(osm.getValue().getSalesDate())
              .withSellingStatusDate(prm.getValue().getSellingStatusDate()).build();

            MergedOrderProductModel mopm = new MergedOrderProductModel();
            MergedOrderProductModelBuilder builder = new MergedOrderProductModelBuilder();
            mopm = builder.withKey(key)
                    .withAudit(audit)
                    .withValue(value)
                    .build();

            ObjectMapper om = appConfig.objectMapper();
            String serialized = om.writeValueAsString(mopm);
            return serialized;
        } catch (JsonProcessingException e) {
            log.error(Throwables.getStackTraceAsString(e));
            throw new RuntimeException(e);
        }

    }
}