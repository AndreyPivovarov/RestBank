package com.example.bankcards.controller;

import com.example.bankcards.dto.BlockRequestCreateDto;
import com.example.bankcards.dto.BlockRequestDecisionDto;
import com.example.bankcards.dto.BlockRequestResponseDto;
import com.example.bankcards.entity.BlockRequest;
import com.example.bankcards.service.BlockRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/block-requests")
public class BlockRequestController {

    private final BlockRequestService blockRequestService;

    @PostMapping
    public BlockRequestResponseDto create(@RequestBody @Valid BlockRequestCreateDto dto,
                                          Principal principal) {

        BlockRequest req = blockRequestService.createRequest(dto.cardId(), principal.getName(), dto.comment());
        return toDto(req);
    }

    @PostMapping("/{id}/approve")
    public BlockRequestResponseDto approve(@PathVariable("id") UUID id,
                                           @RequestBody(required = false) @Valid BlockRequestDecisionDto dto) {

        String comment = dto != null ? dto.comment() : null;
        BlockRequest req = blockRequestService.approve(id, comment);
        return toDto(req);
    }

    @PostMapping("/{id}/reject")
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