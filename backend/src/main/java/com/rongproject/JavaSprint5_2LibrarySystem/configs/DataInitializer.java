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

@Profile("!h2")
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. Initialize Admin
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("111111"));
            admin.setEmail("admin@library.com");
            admin.setUserRole(UserRole.ROLE_ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);
            System.out.println(">>> Root admin initialized: admin/111111");
        }

        // 2. Initialize 20 Books
        if (bookRepository.count() == 0) {
            initializeBooks();
        }
    }

    private void initializeBooks() {
        List<Book> books = Arrays.asList(
                // English Comment: Title, Author, ISBN, Genre, Date, Rating, Stock, Image, Description
                createBook("The Great Gatsby", "F. Scott Fitzgerald", "9780743273565", BookGenre.FICTION, "1925-04-10", 4.4, 10, "the-great-gatsby.jpg", "A classic novel of the Jazz Age."),
                createBook("A Brief History of Time", "Stephen Hawking", "9780553380163", BookGenre.SCIENCE, "1988-04-01", 4.8, 10, "a-brief-history-of-time.jpg", "An exploration of cosmology."),
                createBook("Sapiens", "Yuval Noah Harari", "9780062316097", BookGenre.HISTORY, "2011-01-01", 4.7, 10, "sapiens.jpg", "A brief history of humankind."),
                createBook("The Art Book", "Phaidon Editors", "9780714844879", BookGenre.ART, "2005-04-20", 4.5, 10, "the-art-book.jpg", "An A-Z guide to 500 great painters."),
                createBook("Thinking, Fast and Slow", "Daniel Kahneman", "9780374275631", BookGenre.NON_FICTION, "2011-10-25", 4.6, 10, "thinking-fast-and-slow.jpg", "The psychology of decision-making."),
                createBook("1984", "George Orwell", "9780451524935", BookGenre.FICTION, "1949-06-08", 4.7, 10, "1984.jpg", "A dystopian social science fiction."),
                createBook("Cosmos", "Carl Sagan", "9780345331359", BookGenre.SCIENCE, "1980-10-12", 4.9, 10, "cosmos.jpg", "The story of cosmic evolution."),
                createBook("Guns, Germs, and Steel", "Jared Diamond", "9780393317558", BookGenre.HISTORY, "1997-03-01", 4.4, 10, "guns-germs-and-steel.jpg", "The fates of human societies."),
                createBook("Ways of Seeing", "John Berger", "9780140135152", BookGenre.ART, "1972-12-01", 4.3, 10, "ways-of-seeing.jpg", "Based on the BBC series."),
                createBook("Educated", "Tara Westover", "9780399590504", BookGenre.NON_FICTION, "2018-02-20", 4.7, 10, "educated.jpg", "A memoir about the power of education."),
                createBook("The Hobbit", "J.R.R. Tolkien", "9780547928227", BookGenre.FICTION, "1937-09-21", 4.8, 10, "the-hobbit.jpg", "The precursor to The Lord of the Rings."),
                createBook("The Selfish Gene", "Richard Dawkins", "9780198788607", BookGenre.SCIENCE, "1976-10-14", 4.5, 10, "the-selfish-gene.jpg", "A landmark book on evolution."),
                createBook("The Silk Roads", "Peter Frankopan", "9781101912379", BookGenre.HISTORY, "2015-02-01", 4.5, 10, "the-silk-roads.jpg", "A major new history of the world."),
                createBook("The Story of Art", "E.H. Gombrich", "9780714832470", BookGenre.ART, "1950-01-01", 4.7, 10, "the-story-of-art.jpg", "The most popular art book ever published."),
                createBook("Quiet", "Susan Cain", "9780307352156", BookGenre.NON_FICTION, "2012-01-24", 4.4, 10, "quiet.jpg", "The power of introverts."),
                createBook("Brave New World", "Aldous Huxley", "9780060850524", BookGenre.FICTION, "1932-01-01", 4.3, 10, "brave-new-world.jpg", "A dystopian vision of the future."),
                createBook("Astrophysics", "Neil Tyson", "9780393609394", BookGenre.SCIENCE, "2017-05-02", 4.6, 10, "astrophysics.jpg", "The universe in a nut-shell."),
                createBook("SPQR", "Mary Beard", "9781631492228", BookGenre.HISTORY, "2015-10-20", 4.5, 10, "spqr.jpg", "A history of ancient Rome."),
                createBook("Art Matters", "Neil Gaiman", "9780062906205", BookGenre.ART, "2018-11-20", 4.6, 10, "art-matters.jpg", "Manifesto on the creative arts."),
                createBook("Henrietta Lacks", "Rebecca Skloot", "9781400052189", BookGenre.SCIENCE, "2010-02-02", 4.7, 10, "henrietta-lacks.jpg", "The ethics of science.")
        );
        bookRepository.saveAll(books);
        System.out.println(">>> 20 Books with Full Attributes initialized!");
    }

    private Book createBook(String title, String author, String isbn, BookGenre genre, String date, Double rating, Integer stock, String img, String desc) {
        return Book.builder()
                .title(title)
                .author(author)
                .isbn(isbn)
                .bookGenre(genre)
                .publicationDate(LocalDate.parse(date))
                .rating(rating)
                .availableStock(stock) // English Comment: Inventory added
                .coverImageUrl("/covers/" + img)
                .description(desc)
                .build();
    }
}
