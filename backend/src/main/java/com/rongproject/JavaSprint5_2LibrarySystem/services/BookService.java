package com.rongproject.JavaSprint5_2LibrarySystem.services;

import com.rongproject.JavaSprint5_2LibrarySystem.DTO.BookCreationRequest;
import com.rongproject.JavaSprint5_2LibrarySystem.DTO.BookResponse;
import com.rongproject.JavaSprint5_2LibrarySystem.entities.Book;
import com.rongproject.JavaSprint5_2LibrarySystem.repositories.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public BookResponse createBook(BookCreationRequest request) {
        // Validation: Prevent duplicate ISBN
        if (bookRepository.existsByIsbn(request.isbn())) {
            throw new IllegalStateException("A book with ISBN " + request.isbn() + " already exists.");
        }
        Book book = Book.builder()
                .title(request.title())
                .author(request.author())
                .isbn(request.isbn())
                .bookGenre(request.bookGenre())
                .publicationDate(request.publicationDate())
                .rating(request.rating())
                .description(request.description())
                .availableStock(request.availableStock())
                .coverImageUrl(request.coverImageUrl())
                .build();

        Book savedBook = bookRepository.save(book);
        return mapToResponse(savedBook);
    }
    
    public BookResponse updateBook(Long id, BookCreationRequest request) {
        Book book = bookRepository.getBookById(id);

        // Validation: Prevent duplicate ISBN
        if (!book.getIsbn().equals(request.isbn()) && bookRepository.existsByIsbn(request.isbn())) {
            throw new IllegalStateException("ISBN " + request.isbn() + " is already taken by another book.");
        }

        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setIsbn(request.isbn());
        book.setBookGenre(request.bookGenre());
        book.setPublicationDate(request.publicationDate());
        book.setRating(request.rating());
        book.setDescription(request.description());
        book.setAvailableStock(request.availableStock());
        book.setCoverImageUrl(request.coverImageUrl());

        Book updatedBook = bookRepository.save(book);
        return mapToResponse(updatedBook);
    }

    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public BookResponse getBookById(Long id) {
        Book book = bookRepository.getBookById(id);
        return mapToResponse(book);
    }

    public void deleteBook(Long id) {
        Book book = bookRepository.getBookById(id);
        bookRepository.delete(book);
    }

    private BookResponse mapToResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getBookGenre(),
                book.getPublicationDate(),
                book.getRating(),
                book.getDescription(),
                book.getAvailableStock(),
                book.getCoverImageUrl()
        );
    }
}
