package com.order.join;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class Validator {

    private static final Logger log = LoggerFactory.getLogger(Validator.class);

    private final Validator validator;

    public Validator(Validator validator) {
        this.validator = validator;
    }
/*
    public boolean validateInput(MaterialResponseEvent value) {
        log.debug("Validating input value: {}", value);
        var errors = validator.validate(value);
        if (errors.isEmpty()) {
            log.info("Successfully validated input!!!");
            log.debug("Successfully validated input value: {}", value);
            return ProcessingResult.success(value);
        } else {
            log.error("Failed to validate input value: {} with error: {}", value, toErrorDetails(errors));
            return ProcessingResult.error(toErrorDetails(errors));
        }
    }

    public ProcessingResult<MesMaterialFormedEvent> validateOutput(MesMaterialFormedEvent value) {
        log.debug("Validating output value: {}", value);
        var errors = validator.validate(value);
        if (errors.isEmpty()) {
            log.info("Successfully validated output!!!");
            log.debug("Successfully validated output value: {}", value);
            return ProcessingResult.success(value);
        } else {
            log.error("Failed to validate output value: {} with error: {}", value, toErrorDetails(errors));
            return ProcessingResult.error(toErrorDetails(errors));
        }
    }

    protected static <T> List<Detail> toErrorDetails(Set<ConstraintViolation<T>> violations) {
        return violations.stream()
                .map(constraint -> ErrorUtil.error(StreamsErrorCode.FIELD_VALIDATION_ERROR_CODE,
                        constraint.getPropertyPath().toString() + " " + constraint.getMessage()))
                .collect(toList());
    }
    */
}

