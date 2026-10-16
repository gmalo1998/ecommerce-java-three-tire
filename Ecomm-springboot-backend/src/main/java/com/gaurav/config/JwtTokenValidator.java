
package com.gaurav.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtTokenValidator extends OncePerRequestFilter {

    private static final Logger logger =
            LoggerFactory.getLogger(JwtTokenValidator.class);

    private final SecretKey key = Keys.hmacShaKeyFor(
            JwtConstant.SECRET_KEY.getBytes(StandardCharsets.UTF_8));

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader(JwtConstant.JWT_HEADER);

        // No token: continue; protected endpoints are enforced by Spring Security.
        if (authorizationHeader == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // A supplied Authorization header must use the Bearer scheme.
        if (!authorizationHeader.regionMatches(
                true, 0, "Bearer ", 0, 7)) {
            rejectToken(response);
            return;
        }

        String jwt = authorizationHeader.substring(7).trim();

        if (jwt.isEmpty()) {
            rejectToken(response);
            return;
        }

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();

            String email = claims.get("email", String.class);
            String authorities = claims.get("authorities", String.class);

            if (email == null || email.trim().isEmpty()) {
                rejectToken(response);
                return;
            }

            if (authorities == null) {
                authorities = "";
            }

            List<GrantedAuthority> auths =
                    AuthorityUtils.commaSeparatedStringToAuthorityList(
                            authorities);

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            email, null, auths);

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

            logger.debug("JWT authentication successful");

            filterChain.doFilter(request, response);

        } catch (JwtException | IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();
            rejectToken(response);
        }
    }

    private void rejectToken(HttpServletResponse response)
            throws IOException {

        SecurityContextHolder.clearContext();

        logger.warn("JWT authentication failed: invalid or expired token");

        response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Invalid or expired token");
    }
}