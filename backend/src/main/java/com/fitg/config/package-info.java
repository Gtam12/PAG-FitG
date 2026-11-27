/**
 * Cross-cutting Spring configuration: SecurityConfig, JWT encoder/decoder beans, CORS, RestClient beans.
 */
package com.fitg.config;


/**
 *
 * config/
 *   SecurityConfig.java        ← update the existing one
 *   JwtProperties.java         ← @ConfigurationProperties("fitg.jwt") record(String secret, Duration ttl)
 *   JwtConfig.java             ← JwtEncoder, JwtDecoder, PasswordEncoder beans
 * auth/
 *   controller/AuthController.java
 *   service/AuthService.java
 *   service/JwtService.java    ← builds the token: issuer, subject = user id, issuedAt, expiresAt, email claim
 *   dto/RegisterRequest.java   ← records with @Email @NotBlank @Size
 *   dto/LoginRequest.java
 *   dto/AuthResponse.java      ← record(String token, Instant expiresAt)
 * user/
 *   controller/MeController.java   ← GET /api/me (just this; PATCH comes in BE-03)
 *   dto/UserResponse.java
 * common/
 *   exception/EmailAlreadyUsedException.java   → 409
 *   exception/InvalidCredentialsException.java → 401
 *   GlobalExceptionHandler.java                ← minimal for now; BE-03 expands it
 */