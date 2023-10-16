package com.order.join;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Scanner;

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

    public static String contentOfOld(String resource) {
        // this doesnt work with newlines in json
        String text = new Scanner(INSTANCE.getClass().getResourceAsStream(resource), "UTF-8").useDelimiter("\\A")
                .next();
        return text;
    }

    public static String mockReplacmentOrder_60() throws IOException {
        return contentOf("RawReplacementOrder_60.json");
    }

    public static String mockReplacmentOrderFailureLatest() throws IOException {
        return contentOf("ReplacementOrderFailureLatest.json");
    }

    public static String mockStandardOrder() throws IOException {
        return contentOf("order-create-standard.json");
    }

    public static String mockStandardOrderEvent70() throws IOException {
        return contentOf("order-create-standard-70.json");
    }

}
