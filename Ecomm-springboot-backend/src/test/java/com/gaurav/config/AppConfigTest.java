
package com.gaurav.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.mock.web.MockHttpServletRequest;
class AppConfigTest {

    private final AppConfig appConfig = new AppConfig();

    @Test
    void passwordEncoderShouldHashAndVerifyPassword() {
        PasswordEncoder encoder = appConfig.passwordEncoder();

        String rawPassword = "TestPassword123!";
        String encodedPassword = encoder.encode(rawPassword);

        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(encoder.matches(rawPassword, encodedPassword));
        assertFalse(encoder.matches("WrongPassword", encodedPassword));
    }

    @Test
    void corsShouldAllowFrontendOrigin() {
        CorsConfigurationSource source =
                appConfig.corsConfigurationSource();

        CorsConfiguration cors = source.getCorsConfiguration(
                new MockHttpServletRequest(
                        "GET", "/api/products"
                )
        );

        assertNotNull(cors);
        assertTrue(cors.getAllowedOrigins().contains(
                "https://shopzy.vercel.app"
        ));
        assertTrue(cors.getAllowedMethods().contains("GET"));
        assertTrue(cors.getAllowCredentials());
        assertEquals(
                Long.valueOf(3600),
                cors.getMaxAge()
        );
    }

    @Test
    void corsShouldNotAllowUnknownOrigin() {
        CorsConfigurationSource source =
                appConfig.corsConfigurationSource();

        CorsConfiguration cors = source.getCorsConfiguration(
                new MockHttpServletRequest(
                        "GET", "/api/products"
                )
        );

        assertFalse(cors.getAllowedOrigins().contains(
                "https://untrusted.example"
        ));
    }
}