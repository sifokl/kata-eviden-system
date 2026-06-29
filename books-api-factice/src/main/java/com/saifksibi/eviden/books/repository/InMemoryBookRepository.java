package com.saifksibi.eviden.books.repository;

import com.saifksibi.eviden.books.model.Book;
import com.saifksibi.eviden.books.model.BookCategory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class InMemoryBookRepository {

    private static final List<Book> PUBLIC_BOOKS = List.of(
            new Book(
                    1L,
                    "Public Book - Introduction to APIs",
                    "A public book available without authentication.",
                    BookCategory.PUBLIC,
                    List.of("PUBLIC")
            ),
            new Book(
                    2L,
                    "Public Book - REST Basics",
                    "A public book describing basic REST concepts.",
                    BookCategory.PUBLIC,
                    List.of("PUBLIC")
            )
    );

    private static final List<Book> MEMBERS_BOOKS = List.of(
            new Book(
                    101L,
                    "Members Book - Spring Security Fundamentals",
                    "A members-only book for authenticated users with USER or ADMIN roles.",
                    BookCategory.MEMBERS,
                    List.of("USER", "ADMIN")
            ),
            new Book(
                    102L,
                    "Users Book - OAuth2 Resource Access",
                    "A users/members book explaining protected resource access.",
                    BookCategory.MEMBERS,
                    List.of("USER", "ADMIN")
            )
    );

    private static final List<Book> ADMIN_BOOKS = List.of(
            new Book(
                    201L,
                    "Admin Book - Gateway Security Operations",
                    "An admin-only book for privileged security operations.",
                    BookCategory.ADMIN,
                    List.of("ADMIN")
            ),
            new Book(
                    202L,
                    "Admin Book - IAM Architecture Notes",
                    "An admin-only book about identity and access management architecture.",
                    BookCategory.ADMIN,
                    List.of("ADMIN")
            )
    );

    public List<Book> findPublicBooks() {
        return PUBLIC_BOOKS;
    }

    public List<Book> findMembersBooks() {
        return MEMBERS_BOOKS;
    }

    public List<Book> findAdminBooks() {
        return ADMIN_BOOKS;
    }
}