package org.fromdesertdev.studioops.auth;

import org.fromdesertdev.studioops.user.AppUser;

import java.time.LocalDateTime;

public record AuthUserResponse(
        Long id,
        String name,
        String email,
        LocalDateTime createdAt
) {
    public static AuthUserResponse from(AppUser user) {
        return new AuthUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}
