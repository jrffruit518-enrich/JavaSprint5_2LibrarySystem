package com.rongproject.JavaSprint5_2LibrarySystem.DTO;

import com.rongproject.JavaSprint5_2LibrarySystem.enums.BookGenre;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Schema(description = "Request object for creating a new book")
public record BookCreationRequest(
        @Schema(description = "The title of the book", example = "Effective Java")
        @NotBlank(message = "Title is required")
        String title,

        @Schema(description = "The author of the book", example = "Joshua Bloch")
        @NotBlank(message = "Author is required")
        String author,

        @Schema(description = "ISBN-10 or ISBN-13", example = "9780134685991")
        @NotBlank(message = "ISBN is required")
        @Size(min = 10, max = 13, message = "ISBN should be 10-13 characters")
        String isbn,

        @Schema(description = "The category or genre of the book", example = "TECHNOLOGY")
        @NotNull(message = "Genre is required")
        BookGenre bookGenre,

        @Schema(description = "The date the book was published", example = "2018-01-06")
        @NotNull(message = "Publication date is required")
        LocalDate publicationDate,

        @Schema(description = "Book rating from 0.0 to 5.0", example = "4.9")
        @Min(0) @Max(5)
        Double rating,

        @Schema(description = "Detailed summary of the book", example = "A comprehensive guide to Java best practices.")
        @NotBlank(message = "Description is required")
        String description,

        @Schema(description = "Initial number of copies available in library", example = "5")
        @Min(value = 0, message = "Stock cannot be negative")
        Integer availableStock,

        @Schema(description = "URL of the book cover image", example = "https://example.com/cover.jpg")
        String coverImageUrl
) {
}