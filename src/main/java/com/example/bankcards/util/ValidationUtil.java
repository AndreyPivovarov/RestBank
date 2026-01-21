package com.example.bankcards.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.YearMonth;

@Component
@Slf4j
public class ValidationUtil {

    public boolean isCardExpired(Integer expMonth, Integer expYear) {
        if (expMonth == null || expYear == null) {
            return true;
        }
        YearMonth cardExpiry = YearMonth.of(expYear, expMonth);
        YearMonth now = YearMonth.now();
        return cardExpiry.isBefore(now);
    }

    public boolean isValidExpiry(Integer month, Integer year) {
        if (month == null || year == null) {
            return false;
        }
        return month >= 1 && month <= 12 && year >= 2024 && year <= 2099;
    }

    public boolean isValidAmount(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }
}
