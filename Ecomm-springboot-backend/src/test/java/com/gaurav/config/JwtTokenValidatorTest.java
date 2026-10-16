/*
package com.gaurav.config;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class JwtTokenValidatorTest {

    private JwtTokenValidator filter;
    private SecretKey key;

    @BeforeEach
    void setUp() {
        filter = new JwtTokenValidator();

        key = Keys.hmacShaKeyFor(
                JwtConstant.SECRET_KEY.getBytes(
                        StandardCharsets.UTF_8));

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldContinueWhenAuthorizationHeaderIsMissing()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(200, response.getStatus());
        assertNull(
                SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(chain.getRequest());
    }

    @Test
    void shouldAuthenticateValidToken() throws Exception {

        String token = createToken(
                "ganesh@example.com",
                "ROLE_USER",
                System.currentTimeMillis() + 60_000,
                key);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                JwtConstant.JWT_HEADER, "Bearer " + token);

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertEquals(200, response.getStatus());
        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
        assertEquals("ganesh@example.com",
                authentication.getName());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertNotNull(chain.getRequest());
    }

    @Test
    void shouldRejectMalformedToken() throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                JwtConstant.JWT_HEADER, "Bearer invalid.token");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilter(
                request, response, new MockFilterChain());

        assertEquals(401, response.getStatus());
        assertNull(
                SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldRejectExpiredToken() throws Exception {

        String token = createToken(
                "ganesh@example.com",
                "ROLE_USER",
                System.currentTimeMillis() - 60_000,
                key);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                JwtConstant.JWT_HEADER, "Bearer " + token);

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilter(
                request, response, new MockFilterChain());

        assertEquals(401, response.getStatus());
        assertNull(
                SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldRejectTokenSignedWithDifferentKey()
            throws Exception {

        SecretKey differentKey = Keys.hmacShaKeyFor(
                "different-test-secret-key-with-sufficient-length-12345"
                        .getBytes(StandardCharsets.UTF_8));

        String token = createToken(
                "ganesh@example.com",
                "ROLE_USER",
                System.currentTimeMillis() + 60_000,
                differentKey);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                JwtConstant.JWT_HEADER, "Bearer " + token);

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilter(
                request, response, new MockFilterChain());

        assertEquals(401, response.getStatus());
        assertNull(
                SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldRejectAuthorizationHeaderWithoutBearerScheme()
            throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                JwtConstant.JWT_HEADER, "invalid-header");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilter(
                request, response, new MockFilterChain());

        assertEquals(401, response.getStatus());
    }

    @Test
    void shouldRejectEmptyBearerToken() throws Exception {

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.addHeader(
                JwtConstant.JWT_HEADER, "Bearer ");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        filter.doFilter(
                request, response, new MockFilterChain());

        assertEquals(401, response.getStatus());
    }

    private String createToken(
            String email,
            String authorities,
            long expirationTime,
            SecretKey signingKey) {

        return Jwts.builder()
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000))
                .setExpiration(new Date(expirationTime))
                .claim("email", email)
                .claim("authorities", authorities)
                .signWith(signingKey)
                .compact();
    }
}*/