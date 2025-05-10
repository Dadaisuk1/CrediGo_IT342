package com.credigo.backend.security.oauth2;

import com.credigo.backend.entity.Role;
import com.credigo.backend.entity.User;
import com.credigo.backend.entity.Wallet;
import com.credigo.backend.repository.RoleRepository;
import com.credigo.backend.repository.UserRepository;
import com.credigo.backend.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        logger.info("Processing OAuth2 login for provider: {}", oAuth2UserRequest.getClientRegistration().getRegistrationId());
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (Exception ex) {
            logger.error("Error processing OAuth2 login", ex);
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        // Extract user info from OAuth2 provider
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                oAuth2UserRequest.getClientRegistration().getRegistrationId(),
                oAuth2User.getAttributes()
        );

        // Check if email is available
        if (oAuth2UserInfo.getEmail() == null || oAuth2UserInfo.getEmail().isEmpty()) {
            logger.error("Email not found from OAuth2 provider");
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        logger.info("OAuth2 login attempt for email: {}", oAuth2UserInfo.getEmail());

        // Check if user exists
        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        User user;

        if (userOptional.isPresent()) {
            // Update existing user
            logger.info("Existing user found with email: {}", oAuth2UserInfo.getEmail());
            user = userOptional.get();
            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            // Register new user
            logger.info("Creating new user for email: {}", oAuth2UserInfo.getEmail());
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }

        // Create authorities
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
                .collect(Collectors.toList());

        // Add user ID and username to the attributes map
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("user_id", user.getId());
        attributes.put("username", user.getUsername());
        logger.info("Added user details to OAuth2 attributes - ID: {}, Username: {}",
                user.getId(), user.getUsername());

        // Return OAuth2User with correct attributes and authorities
        return new DefaultOAuth2User(
                authorities,
                attributes,
                "email"
        );
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        User user = new User();

        user.setProvider(oAuth2UserRequest.getClientRegistration().getRegistrationId());
        user.setProviderId(oAuth2UserInfo.getId());
        user.setUsername(oAuth2UserInfo.getName().toLowerCase().replace(" ", "") + UUID.randomUUID().toString().substring(0, 5));
        user.setEmail(oAuth2UserInfo.getEmail());
        user.setImageUrl(oAuth2UserInfo.getImageUrl());
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        // Assign default USER role
        Role userRole = roleRepository.findByRoleName("USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));
        user.setRoles(new HashSet<>(Collections.singletonList(userRole)));

        // Save the user first to get an ID
        user = userRepository.save(user);

        // Create wallet for the new user
        createWalletForUser(user);

        logger.info("New OAuth2 user registered: {} ({})", user.getUsername(), user.getEmail());
        return user;
    }

    private void createWalletForUser(User user) {
        try {
            // Check if wallet already exists
            if (user.getWallet() != null) {
                logger.info("Wallet already exists for user: {}", user.getEmail());
                return;
            }

            // Create new wallet
            Wallet wallet = new Wallet();
            wallet.setUser(user);
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setLastUpdatedAt(LocalDateTime.now());

            wallet = walletRepository.save(wallet);
            user.setWallet(wallet);

            logger.info("Wallet created for user: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Error creating wallet for user: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to create wallet for OAuth2 user", e);
        }
    }

    private User updateExistingUser(User user, OAuth2UserInfo oAuth2UserInfo) {
        user.setImageUrl(oAuth2UserInfo.getImageUrl());

        // Create wallet if it doesn't exist
        if (user.getWallet() == null) {
            createWalletForUser(user);
        }

        logger.info("Existing user updated from OAuth2: {}", user.getEmail());
        return userRepository.save(user);
    }
}
