package com.credigo.backend.repository;

import com.credigo.backend.entity.KYCRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KYCRequestRepository extends JpaRepository<KYCRequest, Long> {
    List<KYCRequest> findByStatus(KYCRequest.Status status);
}
