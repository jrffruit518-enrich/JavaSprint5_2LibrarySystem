package com.rongproject.JavaSprint5_2LibrarySystem.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * AuthResponse DTO
 * This record wraps the JWT token that will be sent back to the client
 * upon successful authentication.
 */
@Schema(description = "Response body containing the authentication token")
public record AuthResponse(
        @Schema(description = "The JSON Web Token (JWT) to be used for authorized requests",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token
) {}
