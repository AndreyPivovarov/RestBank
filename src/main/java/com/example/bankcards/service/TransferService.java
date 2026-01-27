package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransferService {

    public static final String STATUS_ACTIVE = "ACTIVE";

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final ValidationUtil validationUtil;

    @Transactional
    public void transfer(UUID fromCardId, UUID toCardId, BigDecimal amount, String username) {

        if (fromCardId == null || toCardId == null) {
            throw new IllegalArgumentException("Card IDs cannot be null");
        }
        if (fromCardId.equals(toCardId)) {
            throw new IllegalArgumentException("Cannot transfer to the same card");
        }
        if (!validationUtil.isValidAmount(amount)) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        List<Card> cards = cardRepository.findBothByIdForUpdate(fromCardId, toCardId);
        if (cards.size() != 2) {
            throw new ResourceNotFoundException("One or both cards not found");
        }

        Card from = cards.get(0).getId().equals(fromCardId) ? cards.get(0) : cards.get(1);
        Card to = cards.get(0).getId().equals(toCardId) ? cards.get(0) : cards.get(1);

        if (!from.getUser().getId().equals(user.getId()) || !to.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You can transfer only between your own cards");
        }

        if (!STATUS_ACTIVE.equals(from.getStatus()) || !STATUS_ACTIVE.equals(to.getStatus())) {
            throw new IllegalStateException("Both cards must be active");
        }

        if (validationUtil.isCardExpired(from.getExpMonth(), from.getExpYear())
                || validationUtil.isCardExpired(to.getExpMonth(), to.getExpYear())) {
            throw new IllegalStateException("Cannot transfer using expired card");
        }

        BigDecimal newFromBalance = from.getBalance().subtract(amount);
        if (newFromBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }

        from.setBalance(newFromBalance);
        to.setBalance(to.getBalance().add(amount));

        LocalDateTime now = LocalDateTime.now();
        from.setUpdatedAt(now);
        to.setUpdatedAt(now);

        cardRepository.save(from);
        cardRepository.save(to);

        log.info("Transfer {} from {} to {} for user {}", amount, fromCardId, toCardId, username);
    }
}