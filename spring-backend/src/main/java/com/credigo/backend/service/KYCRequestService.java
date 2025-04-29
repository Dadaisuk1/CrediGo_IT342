package com.credigo.backend.service;

import com.credigo.backend.entity.KYCRequest;
import java.util.List;

public interface KYCRequestService {
    List<KYCRequest> findAll();
    KYCRequest approve(Long id, String adminComment);
    KYCRequest reject(Long id, String adminComment);
    void delete(Long id);
}
