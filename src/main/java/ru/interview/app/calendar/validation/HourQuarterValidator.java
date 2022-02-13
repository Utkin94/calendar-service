package ru.interview.app.calendar.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.ZonedDateTime;

import static ru.interview.app.calendar.utils.TimeUtils.isHourQuarter;

public class HourQuarterValidator implements ConstraintValidator<HourQuarter, ZonedDateTime> {

    @Override
    public boolean isValid(ZonedDateTime value, ConstraintValidatorContext context) {
        return value == null || isHourQuarter(value);
    }
}
