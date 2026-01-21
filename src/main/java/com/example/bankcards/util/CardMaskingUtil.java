package com.example.bankcards.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CardMaskingUtil {

    public String extractLast4(String pan) {
        if (pan == null || pan.length() < 4) {
            throw new IllegalArgumentException("PAN must be at least 4 characters");
        }
        return pan.substring(pan.length() - 4);
    }

    public String maskPan(String last4) {
        if (last4 == null || last4.length() != 4) {
            throw new IllegalArgumentException("Last4 must be exactly 4 characters");
        }
        return "**** **** **** " + last4;
    }

    public boolean isValidPan(String pan) {
        return pan != null && pan.matches("\\d{13,19}");
    }
}
