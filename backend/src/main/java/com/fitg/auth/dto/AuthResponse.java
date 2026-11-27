package com.fitg.auth.dto;

import java.time.Instant;

public record AuthResponse(String token, Instant expiresAt) {


}
