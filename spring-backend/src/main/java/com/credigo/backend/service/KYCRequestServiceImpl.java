package com.credigo.backend.service;

import com.credigo.backend.entity.KYCRequest;
import com.credigo.backend.repository.KYCRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class KYCRequestServiceImpl implements KYCRequestService {
    private final KYCRequestRepository kycRequestRepository;

    @Autowired
    public KYCRequestServiceImpl(KYCRequestRepository kycRequestRepository) {
        this.kycRequestRepository = kycRequestRepository;
    }

    @Override
    public List<KYCRequest> findAll() {
        return kycRequestRepository.findAll();
    }

    @Override
    @Transactional
    public KYCRequest approve(Long id, String adminComment) {
        KYCRequest req = kycRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KYC request not found"));
        req.setStatus(KYCRequest.Status.APPROVED);
        req.setReviewedAt(LocalDateTime.now());
        req.setAdminComment(adminComment);
        return kycRequestRepository.save(req);
    }

    @Override
    @Transactional
    public KYCRequest reject(Long id, String adminComment) {
        KYCRequest req = kycRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("KYC request not found"));
        req.setStatus(KYCRequest.Status.REJECTED);
        req.setReviewedAt(LocalDateTime.now());
        req.setAdminComment(adminComment);
        return kycRequestRepository.save(req);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        kycRequestRepository.deleteById(id);
    }
}
