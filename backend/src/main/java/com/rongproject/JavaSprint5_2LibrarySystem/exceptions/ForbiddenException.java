package com.rongproject.JavaSprint5_2LibrarySystem.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Library Project - Exception for Access Denied (403)
 * English Comment: Triggered when a user has a valid token but lacks permission for the specific resource.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
