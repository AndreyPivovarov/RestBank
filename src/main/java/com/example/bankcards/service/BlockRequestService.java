package com.example.bankcards.service;

import com.example.bankcards.entity.BlockRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.BlockRequestRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BlockRequestService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";

    private final BlockRequestRepository blockRequestRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardService cardService;

    @Transactional
    public BlockRequest createRequest(UUID cardId, String username, String comment) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found: " + cardId));

        if (!card.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You can request block only for your own card");
        }

        blockRequestRepository.findTopByCardIdAndStatusOrderByCreatedAtDesc(cardId, STATUS_PENDING)
                .ifPresent(r -> {
                    throw new IllegalStateException("Block request already exists (PENDING)");
                });

        BlockRequest req = new BlockRequest();
        req.setCard(card);
        req.setStatus(STATUS_PENDING);
        req.setComment(comment);

        return blockRequestRepository.save(req);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public BlockRequest approve(UUID requestId, String comment) {

        BlockRequest req = blockRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Block request not found: " + requestId));

        if (!STATUS_PENDING.equals(req.getStatus())) {
            throw new IllegalStateException("Only PENDING request can be approved");
        }

        cardService.blockCard(req.getCard().getId());

        req.setStatus(STATUS_APPROVED);
        req.setComment(comment);

        return blockRequestRepository.save(req);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public BlockRequest reject(UUID requestId, String comment) {

        BlockRequest req = blockRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Block request not found: " + requestId));

        if (!STATUS_PENDING.equals(req.getStatus())) {
            throw new IllegalStateException("Only PENDING request can be rejected");
        }

        req.setStatus(STATUS_REJECTED);
        req.setComment(comment);

        return blockRequestRepository.save(req);
    }
}