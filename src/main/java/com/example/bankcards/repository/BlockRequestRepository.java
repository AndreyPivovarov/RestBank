package com.example.bankcards.repository;

import com.example.bankcards.entity.BlockRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BlockRequestRepository extends JpaRepository<BlockRequest, UUID> {
    Page<BlockRequest> findByCardId(UUID cardId, Pageable pageable);
    Optional<BlockRequest> findTopByCardIdAndStatusOrderByCreatedAtDesc(UUID cardId, String status);
}
