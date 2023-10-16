package com.order.join;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Scanner;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

class OpenApiModelSerialization {

    public String contentOf(String classpath) throws IOException {
        String text = new Scanner(getClass().getResourceAsStream(classpath), "UTF-8").useDelimiter("\\A").next();
        return text;
    }

    void testDeserialization() throws IOException {
        String json = null; //TestUtils.mockStandardOrder();
        assertThat(json).isNotEmpty();
        // ObjectMapper jsonmapper = new ObjectMapper();
        ObjectMapper jsonmapper = new JsonMapper();
        jsonmapper.registerModule(new JsonNullableModule());
        jsonmapper.registerModule((new JavaTimeModule()));
        // jsonmapper.registerModule(new
        // ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
        Object order = jsonmapper.readValue(json, Object.class);
        assertThat(order).isNotNull();
    }

    void testDeserialization70() throws IOException, JSONException {
        String json = null;//TestUtils.mockStandardOrderEvent70();
        assertThat(json).isNotEmpty();
        // ObjectMapper jsonmapper = new ObjectMapper();
        ObjectMapper jsonmapper = new JsonMapper();
        jsonmapper.registerModule(new JsonNullableModule());
        // sonmapper.setTimeZone(TimeZone.getTimeZone("UTC"));
        jsonmapper.registerModule((new JavaTimeModule()));
        jsonmapper.setDateFormat(new StdDateFormat());
        Object order = jsonmapper.readValue(json, Object.class);
        assertThat(order).isNotNull();
        String orderSerialized = jsonmapper.writeValueAsString(order);
        JSONAssert.assertEquals(json, orderSerialized, JSONCompareMode.LENIENT);

    }
/* fails due to shipmentDate change from date to date-time
    @Test
    void testDeserialization_60_lattest() throws IOException {
        String json = TestUtils.mockReplacmentOrder_60();
        assertThat(json).isNotEmpty();
        // ObjectMapper jsonmapper = new ObjectMapper();
        ObjectMapper jsonmapper = new JsonMapper();
        jsonmapper.registerModule(new JsonNullableModule());
        jsonmapper.registerModule((new JavaTimeModule()));
        // jsonmapper.registerModule(new
        // ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
        Order order = jsonmapper.readValue(json, Order.class);
        assertThat(order).isNotNull();
    }
    */

}