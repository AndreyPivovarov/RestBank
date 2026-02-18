package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferRequestDto;
import com.example.bankcards.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/transfers")
@Tag(name = "Transfers", description = "Переводы между картами")
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    @Operation(
            summary = "Выполнить перевод между своими картами",
            description = "Переводит средства с одной карты на другую. Обе карты должны принадлежать текущему пользователю. " +
                    "Карты должны быть активны и иметь достаточный баланс."
    )
    public ResponseEntity<Void> transfer(@RequestBody @Valid TransferRequestDto request,
                                         Principal principal) {

        String username = principal.getName();

        transferService.transfer(
                request.fromCardId(),
                request.toCardId(),
                request.amount(),
                username
        );

        return ResponseEntity.noContent().build();
    }
}
