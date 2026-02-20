package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.ValidationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransferService Unit Tests")
class TransferServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ValidationUtil validationUtil;

    @InjectMocks
    private TransferService transferService;

    private UUID userId;
    private UUID fromCardId;
    private UUID toCardId;

    private User testUser;
    private Card fromCard;
    private Card toCard;

    @BeforeEach
    void setUp() {

        userId = UUID.randomUUID();
        fromCardId = UUID.randomUUID();
        toCardId = UUID.randomUUID();

        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName("ROLE_USER");

        testUser = new User();
        testUser.setId(userId);
        testUser.setUsername("testuser");
        testUser.setRole(role);
        testUser.setEnabled(true);

        fromCard = new Card();
        fromCard.setId(fromCardId);
        fromCard.setUser(testUser);
        fromCard.setStatus("ACTIVE");
        fromCard.setBalance(BigDecimal.valueOf(1000));
        fromCard.setExpMonth(12);
        fromCard.setExpYear(2030);
        fromCard.setUpdatedAt(LocalDateTime.now());

        toCard = new Card();
        toCard.setId(toCardId);
        toCard.setUser(testUser);
        toCard.setStatus("ACTIVE");
        toCard.setBalance(BigDecimal.valueOf(500));
        toCard.setExpMonth(12);
        toCard.setExpYear(2030);
        toCard.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("transfer - должен успешно перевести средства между своими картами")
    void transfer_ShouldTransferSuccessfully() {

        BigDecimal amount = BigDecimal.valueOf(200);

        when(validationUtil.isValidAmount(amount)).thenReturn(true);
        when(validationUtil.isCardExpired(anyInt(), anyInt())).thenReturn(false);
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findBothByIdForUpdate(fromCardId, toCardId))
                .thenReturn(List.of(fromCard, toCard));

        transferService.transfer(fromCardId, toCardId, amount, "testuser");

        ArgumentCaptor<Card> captor = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository, times(2)).save(captor.capture());

        List<Card> savedCards = captor.getAllValues();

        Card savedFrom = savedCards.stream()
                .filter(c -> c.getId().equals(fromCardId))
                .findFirst()
                .orElseThrow();

        Card savedTo = savedCards.stream()
                .filter(c -> c.getId().equals(toCardId))
                .findFirst()
                .orElseThrow();

        assertThat(savedFrom.getBalance())
                .isEqualByComparingTo(BigDecimal.valueOf(800));

        assertThat(savedTo.getBalance())
                .isEqualByComparingTo(BigDecimal.valueOf(700));

        assertThat(savedFrom.getUpdatedAt()).isNotNull();
        assertThat(savedTo.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("transfer - должен выбросить исключение если сумма невалидна")
    void transfer_ShouldThrowException_WhenAmountInvalid() {

        BigDecimal amount = BigDecimal.valueOf(-100);

        when(validationUtil.isValidAmount(amount)).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> transferService.transfer(fromCardId, toCardId, amount, "testuser"));

        verify(cardRepository, never()).save(any());
    }

    @Test
    @DisplayName("transfer - должен выбросить исключение если пользователь не найден")
    void transfer_ShouldThrowException_WhenUserNotFound() {

        BigDecimal amount = BigDecimal.valueOf(100);

        when(validationUtil.isValidAmount(amount)).thenReturn(true);
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transferService.transfer(fromCardId, toCardId, amount, "testuser"));

        verify(cardRepository, never()).save(any());
    }

    @Test
    @DisplayName("transfer - должен выбросить исключение если карты не найдены")
    void transfer_ShouldThrowException_WhenCardsNotFound() {

        BigDecimal amount = BigDecimal.valueOf(100);

        when(validationUtil.isValidAmount(amount)).thenReturn(true);
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findBothByIdForUpdate(fromCardId, toCardId))
                .thenReturn(List.of(fromCard)); // меньше 2

        assertThrows(ResourceNotFoundException.class,
                () -> transferService.transfer(fromCardId, toCardId, amount, "testuser"));

        verify(cardRepository, never()).save(any());
    }

    @Test
    @DisplayName("transfer - должен запретить перевод между чужими картами")
    void transfer_ShouldThrowException_WhenAccessDenied() {

        BigDecimal amount = BigDecimal.valueOf(100);

        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID());

        toCard.setUser(anotherUser);

        when(validationUtil.isValidAmount(amount)).thenReturn(true);
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findBothByIdForUpdate(fromCardId, toCardId))
                .thenReturn(List.of(fromCard, toCard));

        assertThrows(AccessDeniedException.class,
                () -> transferService.transfer(fromCardId, toCardId, amount, "testuser"));

        verify(cardRepository, never()).save(any());
    }

    @Test
    @DisplayName("transfer - должен выбросить исключение если карта просрочена")
    void transfer_ShouldThrowException_WhenCardExpired() {

        BigDecimal amount = BigDecimal.valueOf(100);

        when(validationUtil.isValidAmount(amount)).thenReturn(true);
        when(validationUtil.isCardExpired(anyInt(), anyInt())).thenReturn(true);
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findBothByIdForUpdate(fromCardId, toCardId))
                .thenReturn(List.of(fromCard, toCard));

        assertThrows(IllegalStateException.class,
                () -> transferService.transfer(fromCardId, toCardId, amount, "testuser"));

        verify(cardRepository, never()).save(any());
    }

    @Test
    @DisplayName("transfer - должен выбросить исключение при недостатке средств")
    void transfer_ShouldThrowException_WhenInsufficientFunds() {

        BigDecimal amount = BigDecimal.valueOf(2000);

        when(validationUtil.isValidAmount(amount)).thenReturn(true);
        when(validationUtil.isCardExpired(anyInt(), anyInt())).thenReturn(false);
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(cardRepository.findBothByIdForUpdate(fromCardId, toCardId))
                .thenReturn(List.of(fromCard, toCard));

        assertThrows(IllegalStateException.class,
                () -> transferService.transfer(fromCardId, toCardId, amount, "testuser"));

        verify(cardRepository, never()).save(any());
    }
}