package com.rongproject.JavaSprint5_2LibrarySystem.repositories;

import com.rongproject.JavaSprint5_2LibrarySystem.entities.Book;
import com.rongproject.JavaSprint5_2LibrarySystem.exceptions.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book,Long> {
    Optional<Book> findByTitle(String title);

    // Check if ISBN already exists
    boolean existsByIsbn(String isbn);

    default Book getBookById(Long id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + id));
    }

    @Modifying
    @Query("UPDATE Book b SET b.stock = b.stock - 1 WHERE b.id = :bookId AND b.stock > 0")
// English Comment: Atomically decrement stock if it's greater than 0.
// Returns the number of affected rows (1 if success, 0 if out of stock).
    int decrementStock(@Param("bookId") Long bookId);

}