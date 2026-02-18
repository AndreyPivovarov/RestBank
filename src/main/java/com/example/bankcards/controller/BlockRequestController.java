package com.example.bankcards.controller;

import com.example.bankcards.dto.BlockRequestCreateDto;
import com.example.bankcards.dto.BlockRequestDecisionDto;
import com.example.bankcards.dto.BlockRequestResponseDto;
import com.example.bankcards.entity.BlockRequest;
import com.example.bankcards.service.BlockRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@RequestMapping("/block-requests")
@Tag(name = "Block Requests", description = "Запросы пользователей на блокировку карт")
public class BlockRequestController {

    private final BlockRequestService blockRequestService;

    @PostMapping
    @Operation(
            summary = "Создать запрос на блокировку карты",
            description = "USER создает запрос на блокировку своей карты (например, при утере). " +
                    "Запрос попадает на рассмотрение администратору."
    )
    public BlockRequestResponseDto create(@RequestBody @Valid BlockRequestCreateDto dto,
                                          Principal principal) {

        BlockRequest req = blockRequestService.createRequest(dto.cardId(), principal.getName(), dto.comment());
        return toDto(req);
    }

    @PostMapping("/{id}/approve")
    @Operation(
            summary = "Одобрить запрос на блокировку (только ADMIN)",
            description = "Администратор одобряет запрос. Карта блокируется автоматически."
    )
    public BlockRequestResponseDto approve(@PathVariable("id") UUID id,
                                           @RequestBody(required = false) @Valid BlockRequestDecisionDto dto) {

        String comment = dto != null ? dto.comment() : null;
        BlockRequest req = blockRequestService.approve(id, comment);
        return toDto(req);
    }

    @PostMapping("/{id}/reject")
    @Operation(
            summary = "Отклонить запрос на блокировку (только ADMIN)",
            description = "Администратор отклоняет запрос. Карта остается активной."
    )
    public BlockRequestResponseDto reject(@PathVariable("id") UUID id,
                                          @RequestBody(required = false) @Valid BlockRequestDecisionDto dto) {

        String comment = dto != null ? dto.comment() : null;
        BlockRequest req = blockRequestService.reject(id, comment);
        return toDto(req);
    }

    private BlockRequestResponseDto toDto(BlockRequest req) {
        return new BlockRequestResponseDto(
                req.getId(),
                req.getCard().getId(),
                req.getStatus(),
                req.getComment(),
                req.getCreatedAt()
        );
    }
}