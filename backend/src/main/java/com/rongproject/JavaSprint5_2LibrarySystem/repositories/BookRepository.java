package com.rongproject.JavaSprint5_2LibrarySystem.repositories;

import com.rongproject.JavaSprint5_2LibrarySystem.entities.Book;
import com.rongproject.JavaSprint5_2LibrarySystem.exceptions.ResourceNotFoundException;
import jakarta.transaction.Transactional;
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

    @Transactional // 写入操作必须开启事务
    @Modifying    // 通知 JPA 这是一个更新操作
    @Query("UPDATE Book b SET b.availableStock = b.availableStock - 1 WHERE b.id = :bookId AND b.availableStock > 0")
        // English Comment: Atomically decrement availableStock if it's greater than 0.
    int decrementStock(@Param("bookId") Long bookId);

    @Transactional
    @Modifying
    @Query("UPDATE Book b SET b.availableStock = b.availableStock + 1 WHERE b.id = :bookId")
        // English Comment: Increment availableStock when a book is returned.
    int incrementStock(@Param("bookId") Long bookId);

}