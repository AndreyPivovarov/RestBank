package com.example.bankcards.util;

import com.example.bankcards.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public final class CardNumberGenerator {

    private final CardRepository cardRepository;
    private final CardEncryptionUtil cardEncryptionUtil;
    private static final SecureRandom random = new SecureRandom();
    private static final int CARD_VALIDITY_YEARS = 5;

    public String generateUniquePan() {
        String pan;
        int attempts = 0;
        final int MAX_ATTEMPTS = 100;

        do {
            pan = generatePan();
            attempts++;

            if (attempts > MAX_ATTEMPTS) {
                log.error("Failed to generate unique PAN after {} attempts", MAX_ATTEMPTS);
                throw new IllegalStateException("Unable to generate unique card number");
            }

        } while (panExists(pan));

        log.debug("Generated unique PAN after {} attempts", attempts);
        return pan;
    }

    private String generatePan() {

        String bin = "4400";

        StringBuilder pan = new StringBuilder(bin);

        for (int i = 0; i < 11; i++) {
            pan.append(random.nextInt(10));
        }

        int checkDigit = calculateLuhnCheckDigit(pan.toString());
        pan.append(checkDigit);

        return pan.toString();
    }

    private int calculateLuhnCheckDigit(String panWithoutCheck) {
        int sum = 0;
        boolean alternate = true;

        for (int i = panWithoutCheck.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(panWithoutCheck.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return (10 - (sum % 10)) % 10;
    }

    private boolean panExists(String pan) {
        try {
            String panHash = cardEncryptionUtil.hashPan(pan);
            return cardRepository.existsByPanHash(panHash);
        } catch (Exception e) {
            log.error("Error checking PAN existence", e);
            return true;
        }
    }

    public int[] generateExpirationDate() {
        LocalDate now = LocalDate.now();

        int expMonth = now.getMonthValue();
        int expYear = now.getYear() + CARD_VALIDITY_YEARS;

        return new int[]{expMonth, expYear};
    }
}