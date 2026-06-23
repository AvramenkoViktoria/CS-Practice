package com.naukma.config;

import io.github.cdimascio.dotenv.Dotenv;

public final class Env {

    private static final Dotenv DOTENV = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    private Env() {}

    public static String get(String key) {
        String fromDotenv = DOTENV.get(key);
        return fromDotenv != null ? fromDotenv : System.getenv(key);
    }

    public static String get(String key, String def) {
        String value = get(key);
        return (value != null && !value.isBlank()) ? value : def;
    }

    public static String require(String key) {
        String value = get(key);
        if (value == null || value.isBlank())
            throw new IllegalStateException(
                    "Required configuration '" + key + "' is not set (.env file or environment variable)");
        return value;
    }
}
