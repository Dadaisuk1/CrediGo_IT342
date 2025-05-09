package com.credigo.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/oauth2")
public class OAuth2Controller {

    /**
     * This endpoint is just for documentation purposes.
     * The actual OAuth2 login flow is handled by Spring Security.
     *
     * To initiate Google OAuth2 login, redirect users to:
     * /api/auth/oauth2/authorize/google
     */
    @GetMapping("/info")
    public String getOAuth2Info() {
        return "To login with Google, redirect users to: /api/auth/oauth2/authorize/google";
    }
}
