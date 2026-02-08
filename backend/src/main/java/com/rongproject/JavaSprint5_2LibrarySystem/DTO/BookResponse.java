package com.rongproject.JavaSprint5_2LibrarySystem.DTO;

import com.rongproject.JavaSprint5_2LibrarySystem.enums.BookGenre;

import java.time.LocalDate;

public record BookResponse(
        Long id,
        String title,
        String author,
        String isbn,
        BookGenre bookGenre,
        LocalDate publicationDate,
        Double rating,
        String description,
        Integer availableStock,
        String coverImageUrl // 前端直接拿去渲染图片的 URL
) {}
