package com.fitg.user.dto;

import com.fitg.user.model.UserEntity;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(UUID id, String email, String displayName, Integer dailyCalorieGoal, String timezone, Instant createdAt
) {
    public static UserResponse from(UserEntity u) {
        return new UserResponse(
                u.getId(), u.getEmail(), u.getDisplayName(), u.getDailyCalorieGoal(), u.getTimezone(), u.getCreatedAt()
        );

    }
}
