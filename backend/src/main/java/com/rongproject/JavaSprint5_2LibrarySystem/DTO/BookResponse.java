package com.rongproject.JavaSprint5_2LibrarySystem.DTO;

import com.rongproject.JavaSprint5_2LibrarySystem.enums.BookGenre;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

/**
 * Data Transfer Object representing a book's full details sent to the frontend.
 */
@Schema(description = "Response object containing complete book information")
public record BookResponse(
        @Schema(description = "The unique database ID of the book", example = "1024")
        Long id,

        @Schema(description = "The title of the book", example = "Effective Java")
        String title,

        @Schema(description = "The author of the book", example = "Joshua Bloch")
        String author,

        @Schema(description = "The 10 or 13 digit ISBN", example = "9780134685991")
        String isbn,

        @Schema(description = "The genre/category of the book", example = "TECHNOLOGY")
        BookGenre bookGenre,

        @Schema(description = "The date the book was published", example = "2018-01-06")
        LocalDate publicationDate,

        @Schema(description = "Average user rating (0.0 to 5.0)", example = "4.9")
        Double rating,

        @Schema(description = "A detailed summary of the book content", example = "A comprehensive guide to Java best practices.")
        String description,

        @Schema(description = "Number of copies currently available for borrowing", example = "5")
        Integer availableStock,

        @Schema(description = "The full URL of the cover image for frontend rendering",
                example = "https://example.com/images/effective-java.jpg")
        String coverImageUrl
) {
}
