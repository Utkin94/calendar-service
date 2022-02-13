package ru.interview.app.calendar.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.interview.app.calendar.service.ValidationService;

import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

@Service
@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService {

    private final Validator validator;

    @Override
    public <T> T validate(T objectToValidate) {
        var violations = validator.validate(objectToValidate);

        if (!violations.isEmpty()) {
            var sb = new StringBuilder();
            for (var constraintViolation : violations) {
                sb.append(constraintViolation.getPropertyPath().toString())
                        .append(" ")
                        .append(constraintViolation.getMessage())
                        .append(", ");
            }
            throw new ConstraintViolationException("Error occurred: " + sb, violations);
        }

        return objectToValidate;
    }
}
