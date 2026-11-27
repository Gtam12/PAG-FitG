package com.fitg.user.controller;

import com.fitg.user.dto.UserResponse;
import com.fitg.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
@Tag(name = "me")
public class MeController {

    private final UserService service;


    @GetMapping()
    public UserResponse getMe(@AuthenticationPrincipal Jwt jwt) {
        return service.getMe(UUID.fromString(jwt.getSubject()));
    }
}
