package com.rongproject.JavaSprint5_2LibrarySystem.exceptions;


// Ensure the GlobalExceptionHandler picks this up as 409 Conflict
public class AlreadyExistsException extends RuntimeException {
    public AlreadyExistsException(String message) {
        super(message);
    }
}
