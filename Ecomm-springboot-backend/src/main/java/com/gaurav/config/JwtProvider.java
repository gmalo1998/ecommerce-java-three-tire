
package com.gaurav.config;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtProvider {

    private static final Logger logger =
            LoggerFactory.getLogger(JwtProvider.class);

    private static final long JWT_EXPIRATION_MS = 86_400_000L;

    private final SecretKey key =
            Keys.hmacShaKeyFor(JwtConstant.SECRET_KEY.getBytes(
                    java.nio.charset.StandardCharsets.UTF_8));

    public String generateToken(Authentication auth) {

        Collection<? extends GrantedAuthority> authorities =
                auth.getAuthorities();

        String roles = populateAuthorities(authorities);

        String jwt = Jwts.builder()
                .setIssuedAt(new Date())
                .setExpiration(new Date(
                        System.currentTimeMillis() + JWT_EXPIRATION_MS))
                .claim("email", auth.getName())
                .claim("authorities", roles)
                .signWith(key)
                .compact();

        logger.debug("JWT generated successfully");

        return jwt;
    }

    public String getEmailFromJwtToken(String jwt) {

        if (jwt == null || jwt.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "JWT must not be null or empty");
        }

        String token = jwt.trim();

        if (token.regionMatches(true, 0, "Bearer ", 0, 7)) {
            token = token.substring(7).trim();
        }

        if (token.isEmpty()) {
            throw new IllegalArgumentException(
                    "JWT token is missing");
        }

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String email = claims.get("email", String.class);

            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "JWT email claim is missing");
            }

            return email;

        } catch (JwtException | IllegalArgumentException exception) {
            logger.warn("JWT validation failed: invalid or expired token");
            throw exception;
        }
    }

    public String populateAuthorities(
            Collection<? extends GrantedAuthority> collection) {

        Set<String> auths = new HashSet<>();

        for (GrantedAuthority authority : collection) {
            auths.add(authority.getAuthority());
        }

        return String.join(",", auths);
    }
}