package com.order.join;

import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import static org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.*;

/**
 * Exception handling Approach taken from Confluent web site: 
 * https://developer.confluent.io/tutorials/error-handling/kstreams.html
 */
public class ExceptionHandler implements StreamsUncaughtExceptionHandler {

    final int maxFailures;
    final long maxTimeIntervalMillis;
    private Instant previousErrorTime;
    private int currentFailureCount;


    public ExceptionHandler(final int maxFailures, final long maxTimeIntervalMillis) {
        this.maxFailures = maxFailures;
        this.maxTimeIntervalMillis = maxTimeIntervalMillis;
    }

    @Override
    public StreamThreadExceptionResponse handle(final Throwable throwable) {
        currentFailureCount++;
        Instant currentErrorTime = Instant.now();

        if (previousErrorTime == null) {
            previousErrorTime = currentErrorTime;
        }

        long millisBetweenFailure = ChronoUnit.MILLIS.between(previousErrorTime, currentErrorTime);

        if (currentFailureCount >= maxFailures) {
            if (millisBetweenFailure <= maxTimeIntervalMillis) {
                return SHUTDOWN_APPLICATION;
            } else {
                currentFailureCount = 0;
                previousErrorTime = null;
            }
        }
        return REPLACE_THREAD;
    }
}
