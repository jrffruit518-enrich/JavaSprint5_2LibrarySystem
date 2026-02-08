package com.rongproject.JavaSprint5_2LibrarySystem.entities;

import com.rongproject.JavaSprint5_2LibrarySystem.enums.BookGenre;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 255)
    private String author;

    @Column(unique = true, nullable = false, length = 13)
    private String isbn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookGenre bookGenre;

    @Column(nullable = false)
    private LocalDate publicationDate;

    @DecimalMin("0.0")
    @DecimalMax("5.0")
    @Column(nullable = false)
    private Double rating;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Min(0)
    @Column(nullable = false)
    private Integer availableStock = 1;


    private String coverImageUrl;

}
