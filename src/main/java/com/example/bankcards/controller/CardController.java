package com.example.bankcards.controller;

import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@RequestMapping("/cards")
@Tag(name = "Cards", description = "Управление банковскими картами")
public class CardController {

    private final CardService cardService;

    @GetMapping("/{id}")
    @Operation(
            summary = "Получить карту по ID карты",
            description = "Возвращает информацию о карте. USER может видеть только свои карты, ADMIN - любые."
    )
    public Card getById(@PathVariable UUID id, Principal principal) {
        return cardService.getCardById(id, principal.getName());
    }

    @GetMapping
    @Operation(
            summary = "Получить список карт пользователя",
            description = "Возвращает постраничный список карт. USER видит только свои карты, ADMIN может указать любого пользователя."
    )
    public Page<Card> getUserCards(@RequestParam("userId") UUID userId,
                                   Pageable pageable,
                                   Principal principal) {
        return cardService.getUserCards(userId, principal.getName(), pageable);
    }

    @PostMapping
    @Operation(
            summary = "Создать новую карту (только ADMIN)",
            description = "Создает новую банковскую карту для указанного пользователя. Номер карты генерируется автоматически и шифруется."
    )
    public Card create(@RequestBody @Valid CreateCardRequest req) {
        return cardService.createCard(req.userId(), req.ownerName());
    }

    @PostMapping("/{id}/block")
    @Operation(
            summary = "Заблокировать карту (только ADMIN)",
            description = "Меняет статус карты на BLOCKED. Заблокированной картой нельзя пользоваться."
    )
    public Card block(@PathVariable UUID id) {
        return cardService.blockCard(id);
    }

    @PostMapping("/{id}/unblock")
    @Operation(
            summary = "Разблокировать карту (только ADMIN)",
            description = "Меняет статус карты на ACTIVE. Разблокировать можно только действующую (не истекшую) карту."
    )
    public Card unblock(@PathVariable UUID id) {
        return cardService.unblockCard(id);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Удалить карту (только ADMIN)",
            description = "Удаляет карту из системы. Можно удалить только карту с нулевым балансом."
    )
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}
