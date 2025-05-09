package com.credigo.backend.security;

import com.credigo.backend.entity.Wallet;
import com.credigo.backend.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Optional;

/**
 * Component that provides authorization checks for wallet operations.
 * Used in @PreAuthorize expressions.
 */
@Component("walletSecurity")
public class WalletSecurity {

    private final WalletRepository walletRepository;

    @Autowired
    public WalletSecurity(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    /**
     * Check if the authenticated user is the owner of the wallet
     * 
     * @param walletId The wallet ID
     * @param username The username to check
     * @return true if the user is the owner, false otherwise
     */
    public boolean isWalletOwner(Long walletId, String username) {
        if (walletId == null || username == null || username.isEmpty()) {
            return false;
        }
        
        Optional<Wallet> wallet = walletRepository.findById(walletId);
        return wallet.isPresent() && 
               wallet.get().getUser() != null && 
               username.equals(wallet.get().getUser().getUsername());
    }
} 