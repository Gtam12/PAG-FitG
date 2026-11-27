package com.fitg.auth.service;

import com.fitg.auth.dto.AuthResponse;
import com.fitg.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;
    private final Clock clock;

    public AuthResponse issue(UUID uuid) {
        Instant now = Instant.now(clock);
        Instant expiresAt = now.plus(jwtProperties.ttl());

        //building claims set
        JwtClaimsSet claimsSet = JwtClaimsSet.builder().issuer("fitg")
                .subject(uuid.toString())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .build();

        //Encode
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        //token
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claimsSet)).getTokenValue();

        return new  AuthResponse(token, expiresAt);



    }


}
