package com.marvel.reservation.validators;

import com.marvel.reservation.dto.ReservationRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.temporal.ChronoUnit;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, ReservationRequest> {
    @Override
    public boolean isValid(ReservationRequest request, ConstraintValidatorContext context) {
        if (request.getStartDate() == null || request.getEndDate() == null) {
            return true; // let @NotNull handle this
        }
        long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        return days > 0 && days <= 30;
    }
}