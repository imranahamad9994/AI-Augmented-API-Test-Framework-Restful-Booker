package com.restfulbooker.base;

public final class ApiConfig {

    public static final String BASE_URL = "https://restful-booker.herokuapp.com";
    public static final String PING_ENDPOINT = "/ping";
    public static final String AUTH_ENDPOINT = "/auth";
    public static final String BOOKING_ENDPOINT = "/booking";
    public static final long MAX_RESPONSE_TIME_MS = 3000L;
    public static final String VALID_USERNAME = "admin";
    public static final String VALID_PASSWORD = "password123";
    public static final String INVALID_USERNAME = "wronguser";
    public static final String INVALID_PASSWORD = "wrongpass";
    public static final String BAD_CREDENTIALS_REASON = "Bad credentials";

    private ApiConfig() {
    }
}
