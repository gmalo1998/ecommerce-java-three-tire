/*
package com.gaurav.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class JwtProviderTest {

    private JwtProvider jwtProvider;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider();
        authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("ganesh@example.com");

        doReturn(Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
        )).when(authentication).getAuthorities();
    }

    @Test
    void shouldGenerateTokenSuccessfully() {
        String token = jwtProvider.generateToken(authentication);

        assertNotNull(token);
        assertFalse(token.isBlank());

        assertEquals(
                "ganesh@example.com",
                jwtProvider.getEmailFromJwtToken(token)
        );
    }

    @Test
    void shouldExtractEmailFromBearerToken() {
        String token = jwtProvider.generateToken(authentication);

        String email = jwtProvider.getEmailFromJwtToken(
                "Bearer " + token
        );

        assertEquals("ganesh@example.com", email);
    }

    @Test
    void shouldRejectNullToken() {
        assertThrows(
                IllegalArgumentException.class,
                () -> jwtProvider.getEmailFromJwtToken(null)
        );
    }

    @Test
    void shouldRejectEmptyToken() {
        assertThrows(
                IllegalArgumentException.class,
                () -> jwtProvider.getEmailFromJwtToken(" ")
        );
    }

    @Test
    void shouldRejectMalformedToken() {
        assertThrows(
                JwtException.class,
                () -> jwtProvider.getEmailFromJwtToken("invalid.token.value")
        );
    }

    @Test
    void shouldRejectExpiredToken() {
        SecretKey key = Keys.hmacShaKeyFor(
                JwtConstant.SECRET_KEY.getBytes(
                        StandardCharsets.UTF_8
                )
        );

        String expiredToken = Jwts.builder()
                .claim("email", "ganesh@example.com")
                .setIssuedAt(
                        new Date(System.currentTimeMillis() - 120_000)
                )
                .setExpiration(
                        new Date(System.currentTimeMillis() - 60_000)
                )
                .signWith(key)
                .compact();

        assertThrows(
                JwtException.class,
                () -> jwtProvider.getEmailFromJwtToken(expiredToken)
        );
    }

    @Test
    void shouldRejectTokenSignedWithDifferentKey() {
        SecretKey differentKey = Keys.hmacShaKeyFor(
                "a-different-test-secret-key-with-enough-bytes-123456"
                        .getBytes(StandardCharsets.UTF_8)
        );

        String token = Jwts.builder()
                .claim("email", "ganesh@example.com")
                .setExpiration(
                        new Date(System.currentTimeMillis() + 60_000)
                )
                .signWith(differentKey)
                .compact();

        assertThrows(
                JwtException.class,
                () -> jwtProvider.getEmailFromJwtToken(token)
        );
    }

    @Test
    void shouldRejectNullAuthentication() {
        assertThrows(
                IllegalArgumentException.class,
                () -> jwtProvider.generateToken(null)
        );
    }

    @Test
    void shouldPopulateAuthorities() {
        String authorities = jwtProvider.populateAuthorities(
                Arrays.asList(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                )
        );

        assertTrue(authorities.contains("ROLE_USER"));
        assertTrue(authorities.contains("ROLE_ADMIN"));

        assertEquals(
                1,
                authorities.chars()
                        .filter(character -> character == ',')
                        .count()
        );
    }
}

*/