package com.gaurav.config;

public final class JwtConstant {

    private JwtConstant() {
    }

    public static final String JWT_HEADER = "Authorization";
    public static final String SECRET_KEY = requireSecret();

    private static String requireSecret() {
        String secret = System.getenv("JWT_SECRET_KEY");

        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                "JWT_SECRET_KEY environment variable is missing"
            );
        }

        return secret;
    }
}