package com.project.fitness.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private static final String SECRET = "0123456789012345678901234567890123456789";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 3600000);
    }

    @Test
    void testGenerateAndExtractUsername() {
        UserDetails user = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        String token = jwtService.generateToken(user);
        assertNotNull(token);
        assertEquals("test@example.com", jwtService.extractUsername(token));
        assertTrue(jwtService.isValid(token, user));
    }

    @Test
    void testInvalidTokenWithDifferentUser() {
        UserDetails user1 = User.builder()
                .username("user1@example.com")
                .password("password")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        UserDetails user2 = User.builder()
                .username("user2@example.com")
                .password("password")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        String token = jwtService.generateToken(user1);
        assertFalse(jwtService.isValid(token, user2));
    }

    @Test
    void testExpiredToken() {
        JwtService shortLivedJwtService = new JwtService(SECRET, -1000);
        UserDetails user = User.builder()
                .username("expired@example.com")
                .password("password")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        String token = shortLivedJwtService.generateToken(user);
        assertFalse(shortLivedJwtService.isValid(token, user));
    }
}
