package com.rongproject.JavaSprint5_2LibrarySystem.configs;

import com.rongproject.JavaSprint5_2LibrarySystem.entities.Book;
import com.rongproject.JavaSprint5_2LibrarySystem.entities.User;
import com.rongproject.JavaSprint5_2LibrarySystem.enums.BookGenre;
import com.rongproject.JavaSprint5_2LibrarySystem.enums.UserRole;
import com.rongproject.JavaSprint5_2LibrarySystem.repositories.BookRepository;
import com.rongproject.JavaSprint5_2LibrarySystem.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
@Profile("h2")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initUsers();
        initBooks();
    }

    private void initUsers() {
        if (userRepository.count() == 0) {
            // 1 Admin
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@library.com");
            admin.setUserRole(UserRole.ROLE_ADMIN);
            admin.setEnabled(true);

            // 2 Users
            User user1 = new User();
            user1.setUsername("alice");
            user1.setPassword(passwordEncoder.encode("user123"));
            user1.setEmail("alice@example.com");
            user1.setUserRole(UserRole.ROLE_USER);
            user1.setEnabled(true);

            User user2 = new User();
            user2.setUsername("bob");
            user2.setPassword(passwordEncoder.encode("user123"));
            user2.setEmail("bob@example.com");
            user2.setUserRole(UserRole.ROLE_USER);
            user2.setEnabled(true);

            userRepository.saveAll(List.of(admin, user1, user2));
            System.out.println(">> H2: 1 Admin and 2 Users created.");
        }
    }

    private void initBooks() {
        if (bookRepository.count() == 0) {
            List<Book> books = Arrays.asList(
                    createBook("Effective Java", "Joshua Bloch", "9780134685991", BookGenre.SCIENCE, LocalDate.of(2018, 1, 6), 4.9, "The definitive guide to Java best practices.", 5),
                    createBook("Clean Code", "Robert C. Martin", "9780132350884", BookGenre.SCIENCE, LocalDate.of(2008, 8, 11), 4.8, "A handbook of agile software craftsmanship.", 3),
                    createBook("The Great Gatsby", "F. Scott Fitzgerald", "9780743273565", BookGenre.FICTION, LocalDate.of(1925, 4, 10), 4.4, "A classic novel about the American Dream.", 2),
                    createBook("Sapiens", "Yuval Noah Harari", "9780062316097", BookGenre.HISTORY, LocalDate.of(2011, 1, 1), 4.7, "A brief history of humankind.", 10),
                    createBook("Dune", "Frank Herbert", "9780441172719", BookGenre.FICTION, LocalDate.of(1965, 8, 1), 4.6, "The epic science fiction masterpiece.", 4),
                    createBook("Thinking, Fast and Slow", "Daniel Kahneman", "9780374275631", BookGenre.FICTION, LocalDate.of(2011, 10, 25), 4.5, "Insightful analysis of human thought processes.", 6),
                    createBook("The Silent Patient", "Alex Michaelides", "9781250301697", BookGenre.FICTION, LocalDate.of(2019, 2, 5), 4.3, "A shocking psychological thriller.", 8),
                    createBook("Atomic Habits", "James Clear", "9780735211292", BookGenre.HISTORY, LocalDate.of(2018, 10, 16), 4.9, "An easy way to build good habits.", 15),
                    createBook("The Hobbit", "J.R.R. Tolkien", "9780547928227", BookGenre.NON_FICTION, LocalDate.of(1937, 9, 21), 4.8, "The prelude to The Lord of the Rings.", 7),
                    createBook("The Alchemist", "Paulo Coelho", "9780062315007", BookGenre.FICTION, LocalDate.of(1988, 1, 1), 4.7, "A mystical story of following your destiny.", 12)
            );
            bookRepository.saveAll(books);
            System.out.println(">> H2: 10 diverse books populated.");
        }
    }

    private Book createBook(String title, String author, String isbn, BookGenre genre,
                            LocalDate pubDate, Double rating, String desc, Integer stock) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setBookGenre(genre);
        book.setPublicationDate(pubDate);
        book.setRating(rating);
        book.setDescription(desc);
        book.setAvailableStock(stock);
        // Cover URL can be a placeholder
        book.setCoverImageUrl("https://via.placeholder.com/150x200?text=" + title.replace(" ", "+"));
        return book;
    }
}