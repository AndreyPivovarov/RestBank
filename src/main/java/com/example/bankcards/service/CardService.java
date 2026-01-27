package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardNumberGenerator cardNumberGenerator;
    private final CardEncryptionUtil cardEncryptionUtil;
    private final CardMaskingUtil cardMaskingUtil;
    private final ValidationUtil validationUtil;

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_BLOCKED = "BLOCKED";

    @Transactional
    public Card createCard(UUID userId, String username, String ownerName) {
        log.info("Creating new card for user ID: {}", userId);

        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (ownerName == null || ownerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Owner name cannot be empty");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be empty");
        }

        requireAdmin(username);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        String pan = cardNumberGenerator.generateUniquePan();
        log.debug("Generated PAN for user {}", userId);

        int[] expiration = cardNumberGenerator.generateExpirationDate();
        int expMonth = expiration[0];
        int expYear = expiration[1];

        String panEncrypted = cardEncryptionUtil.encryptPan(pan);

        String panHash = cardEncryptionUtil.hashPan(pan);

        String panLast4 = cardMaskingUtil.extractLast4(pan);

        Card card = new Card();
        card.setUser(user);
        card.setOwnerName(ownerName.trim());
        card.setPanEncrypted(panEncrypted);
        card.setPanHash(panHash);
        card.setPanLast4(panLast4);
        card.setExpMonth(expMonth);
        card.setExpYear(expYear);
        card.setStatus(STATUS_ACTIVE);
        card.setBalance(BigDecimal.ZERO);
        card.setCreatedAt(LocalDateTime.now());
        card.setUpdatedAt(LocalDateTime.now());

        Card savedCard = cardRepository.save(card);
        log.info("Card created successfully with ID: {} for user: {}", savedCard.getId(), userId);

        return savedCard;
    }

    @Transactional(readOnly = true)
    public Card getCardById(UUID cardId, String username) {
        log.debug("Fetching card with ID: {} for user: {}", cardId, username);

        if (cardId == null) {
            throw new IllegalArgumentException("Card ID cannot be null");
        }

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + cardId));

        validateCardAccess(card, username);

        return card;
    }

    @Transactional(readOnly = true)
    public Page<Card> getUserCards(UUID userId, String username, Pageable pageable) {
        log.debug("Fetching cards for user ID: {}", userId);

        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!user.getUsername().equals(username) && !SecurityUtil.isAdmin()) {
            throw new AccessDeniedException("You can only view your own cards");
        }

        return cardRepository.findByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Card> getAllCards(Pageable pageable) {
        log.debug("Fetching all cards with pagination");
        return cardRepository.findAll(pageable);
    }

    @Transactional
    public Card blockCard(UUID cardId, String username) {
        log.info("Blocking card with ID: {}", cardId);

        if (cardId == null) {
            throw new IllegalArgumentException("Card ID cannot be null");
        }

        requireAdmin(username);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + cardId));

        if (STATUS_BLOCKED.equals(card.getStatus())) {
            throw new IllegalStateException("Card is already blocked");
        }

        card.setStatus(STATUS_BLOCKED);
        card.setUpdatedAt(LocalDateTime.now());

        Card updatedCard = cardRepository.save(card);
        log.info("Card blocked successfully: {}", cardId);

        return updatedCard;
    }

    @Transactional
    public Card unblockCard(UUID cardId, String username) {
        log.info("Unblocking card with ID: {}", cardId);

        if (cardId == null) {
            throw new IllegalArgumentException("Card ID cannot be null");
        }

        requireAdmin(username);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + cardId));

        if (STATUS_ACTIVE.equals(card.getStatus())) {
            throw new IllegalStateException("Card is already active");
        }

        if (validationUtil.isCardExpired(card.getExpMonth(), card.getExpYear())) {
            throw new IllegalStateException("Cannot unblock expired card");
        }

        card.setStatus(STATUS_ACTIVE);
        card.setUpdatedAt(LocalDateTime.now());

        Card updatedCard = cardRepository.save(card);
        log.info("Card unblocked successfully: {}", cardId);

        return updatedCard;
    }

    @Transactional
    public void deleteCard(UUID cardId, String username) {
        log.info("Deleting card with ID: {}", cardId);

        if (cardId == null) {
            throw new IllegalArgumentException("Card ID cannot be null");
        }

        requireAdmin(username);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + cardId));

        if (card.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("Cannot delete card with non-zero balance");
        }

        cardRepository.delete(card);
        log.info("Card deleted successfully: {}", cardId);
    }

    private Card updateBalance(Card card, BigDecimal amount) {

        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }

        if (!STATUS_ACTIVE.equals(card.getStatus())) {
            throw new IllegalStateException("Cannot update balance for non-active card");
        }

        if (validationUtil.isCardExpired(card.getExpMonth(), card.getExpYear())) {
            throw new IllegalStateException("Cannot update balance for expired card");
        }

        BigDecimal newBalance = card.getBalance().add(amount);

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }

        card.setBalance(newBalance);
        card.setUpdatedAt(LocalDateTime.now());

        Card updatedCard = cardRepository.save(card);
        log.debug("Balance updated successfully. New balance: {}", newBalance);

        return updatedCard;
    }

    @Transactional
    public Card depositBalance(UUID cardId, BigDecimal amount, String username) {
        log.info("Depositing {} to card {}", amount, cardId);

        if (!validationUtil.isValidAmount(amount)) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Card card = getCardById(cardId, username);

        return updateBalance(card, amount);
    }

    @Transactional(readOnly = true)
    public String getMaskedCardNumber(UUID cardId, String username) {
        Card card = getCardById(cardId, username);
        return cardMaskingUtil.maskPan(card.getPanLast4());
    }

    private void validateCardAccess(Card card, String username) {
        if (SecurityUtil.isAdmin()) {
            return;
        }
        if (!card.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You don't have permission to access this card");
        }
    }

    private void requireAdmin(String username) {
        if (!SecurityUtil.isAdmin()) {
            throw new AccessDeniedException("Admin privileges required");
        }
    }
}