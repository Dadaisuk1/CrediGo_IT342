package com.credigo.backend.controller;

import com.credigo.backend.entity.KYCRequest;
import com.credigo.backend.service.KYCRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/kyc")
public class AdminKYCController {
    private final KYCRequestService kycRequestService;

    @Autowired
    public AdminKYCController(KYCRequestService kycRequestService) {
        this.kycRequestService = kycRequestService;
    }

    // List all KYC requests
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<KYCRequest>> getAll() {
        return ResponseEntity.ok(kycRequestService.findAll());
    }

    // Approve KYC request
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<KYCRequest> approve(@PathVariable Long id, @RequestParam(required = false) String comment) {
        return ResponseEntity.ok(kycRequestService.approve(id, comment));
    }

    // Reject KYC request
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<KYCRequest> reject(@PathVariable Long id, @RequestParam(required = false) String comment) {
        return ResponseEntity.ok(kycRequestService.reject(id, comment));
    }

    // Delete KYC request
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        kycRequestService.delete(id);
        return ResponseEntity.ok().build();
    }
}
