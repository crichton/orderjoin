package com.order.join;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class TestUtils {

    public static TestUtils INSTANCE = new TestUtils();

    public static String contentOf(String fileName) {
        try {
            Object o = Objects.requireNonNull(INSTANCE.getClass().getClassLoader().getResource(fileName),
                    String.format("file %s is null", fileName));
            return Files.readString(Paths
                    .get(Objects.requireNonNull(INSTANCE.getClass().getClassLoader().getResource(fileName)).toURI()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ObjectMapper jsonMapper() {
        var om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        om.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return om;
    }

    public static String order_sale_valid() throws IOException {
        return contentOf("order-sale-model-valid.json");
    }

    public static String order_sale_invalid() throws IOException {
        return contentOf("order-sale-model-invalid.json");
    }

    public static String product_valid() throws IOException {
        return contentOf("product-model-valid.json");
    }

    public static String merged_order_product_valid() throws IOException {
        return contentOf("merged-order-product-model-valid.json");
    }

}
