package com.example.bankcards.controller;

import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cards")
public class CardController {

    private final CardService cardService;

    // USER или ADMIN (ADMIN сможет смотреть любые через SecurityUtil.isAdmin в сервисе)
    @GetMapping("/{id}")
    public Card getById(@PathVariable UUID id, Principal principal) {
        return cardService.getCardById(id, principal.getName());
    }

    // USER: только свои, ADMIN: любые (проверка в сервисе)
    @GetMapping
    public Page<Card> getUserCards(@RequestParam("userId") UUID userId,
                                   Pageable pageable,
                                   Principal principal) {
        return cardService.getUserCards(userId, principal.getName(), pageable);
    }

    // ADMIN-only (CardService уже помечен @PreAuthorize)
    @PostMapping
    public Card create(@RequestBody @Valid CreateCardRequest req) {
        return cardService.createCard(req.userId(), req.ownerName());
    }

    // ADMIN-only
    @PostMapping("/{id}/block")
    public Card block(@PathVariable UUID id) {
        return cardService.blockCard(id);
    }

    // ADMIN-only
    @PostMapping("/{id}/unblock")
    public Card unblock(@PathVariable UUID id) {
        return cardService.unblockCard(id);
    }

    // ADMIN-only
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}
