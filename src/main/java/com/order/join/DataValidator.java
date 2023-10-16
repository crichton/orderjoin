package com.order.join;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.order.join.models.MergedOrderProductModel;
import com.order.join.models.OrderSaleModel;
import com.order.join.models.ProductRegistrationModel;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class DataValidator {

    private static final Logger log = LoggerFactory.getLogger(DataValidator.class);

    private static final Validator VALIDATOR; 

    static {
        VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private DataValidator() {
    }

    public static <T> boolean  validate(T value) {
        log.debug("Validating output value: {}", value);
        var errors = VALIDATOR.validate(value);
        if (errors.isEmpty()) {
            log.info("Successfully validated output!!!");
            log.debug("Successfully validated output value: {}", value);
            return true;
        } else {
            log.error("Failed to validate output value: {} with error: {}", value, toErrorDetails(errors));
            return false;
        }
    }

    protected static <T> List<String> toErrorDetails(Set<ConstraintViolation<T>> violations) {
        return violations.stream()
                .map(constraint -> String.format("validationError[%s %s]%n", constraint.getPropertyPath().toString(), constraint.getMessage()))
                .collect(toList());
    }

}

