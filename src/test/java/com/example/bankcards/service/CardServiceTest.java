package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardEncryptionUtil;
import com.example.bankcards.util.CardMaskingUtil;
import com.example.bankcards.util.CardNumberGenerator;
import com.example.bankcards.util.ValidationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardService Unit Tests")
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardNumberGenerator cardNumberGenerator;

    @Mock
    private CardEncryptionUtil cardEncryptionUtil;

    @Mock
    private CardMaskingUtil cardMaskingUtil;

    @Mock
    private ValidationUtil validationUtil;

    @InjectMocks
    private CardService cardService;

    private UUID userId;
    private UUID cardId;
    private UUID roleId;
    private User testUser;
    private Card testCard;
    private Role testRole;

    @BeforeEach
    void setUp() {

        userId = UUID.randomUUID();
        cardId = UUID.randomUUID();
        roleId = UUID.randomUUID();

        testRole = new Role();
        testRole.setId(roleId);
        testRole.setName("ROLE_USER");

        testUser = new User();
        testUser.setId(userId);
        testUser.setUsername("testuser");
        testUser.setPasswordHash("hashed_password");
        testUser.setRole(testRole);
        testUser.setEnabled(true);
        testUser.setCreatedAt(LocalDateTime.now());

        testCard = new Card();
        testCard.setId(cardId);
        testCard.setUser(testUser);
        testCard.setOwnerName("Test Owner");
        testCard.setPanEncrypted("encrypted_pan");
        testCard.setPanHash("hashed_pan");
        testCard.setPanLast4("1234");
        testCard.setExpMonth(12);
        testCard.setExpYear(2026);
        testCard.setStatus("ACTIVE");
        testCard.setBalance(BigDecimal.valueOf(1000));
        testCard.setCreatedAt(LocalDateTime.now());
        testCard.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("createCard - должен успешно создать карту")
    void createCard_ShouldCreateCardSuccessfully() {
        String ownerName = "John";
        String generatedPan = "4111111111111111";
        String encryptedPan = "encrypted_pan_123";
        String hashedPan = "hashed_pan_456";
        String last4 = "1111";
        int[] expiration = {12, 2026};

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(testUser));

        when(cardNumberGenerator.generateUniquePan())
                .thenReturn(generatedPan);

        when(cardNumberGenerator.generateExpirationDate())
                .thenReturn(expiration);

        when(cardEncryptionUtil.encryptPan(generatedPan))
                .thenReturn(encryptedPan);

        when(cardEncryptionUtil.hashPan(generatedPan))
                .thenReturn(hashedPan);

        when(cardMaskingUtil.extractLast4(generatedPan))
                .thenReturn(last4);

        when(cardRepository.save(any(Card.class)))
                .thenAnswer(invocation -> {
                    Card savedCard = invocation.getArgument(0);
                    savedCard.setId(cardId);
                    return savedCard;
                });

        Card result = cardService.createCard(userId, ownerName);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(cardId);
        assertThat(result.getOwnerName()).isEqualTo(ownerName);
        assertThat(result.getPanEncrypted()).isEqualTo(encryptedPan);
        assertThat(result.getPanHash()).isEqualTo(hashedPan);
        assertThat(result.getPanLast4()).isEqualTo(last4);
        assertThat(result.getExpMonth()).isEqualTo(12);
        assertThat(result.getExpYear()).isEqualTo(2026);
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getUser()).isEqualTo(testUser);

        verify(userRepository, times(1)).findById(userId);

        verify(cardNumberGenerator, times(1)).generateUniquePan();

        verify(cardRepository, times(1)).save(any(Card.class));
    }
}