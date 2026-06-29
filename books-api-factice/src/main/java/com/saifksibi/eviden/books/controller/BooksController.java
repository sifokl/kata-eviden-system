package com.saifksibi.eviden.books.controller;

import com.saifksibi.eviden.books.model.Book;
import com.saifksibi.eviden.books.repository.InMemoryBookRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(
        name = "Books API",
        description = "Fake backend API used as protected resource behind the authentication gateway"
)
public class BooksController {

    private final InMemoryBookRepository bookRepository;

    public BooksController(InMemoryBookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Operation(
            summary = "Get public books",
            description = "Returns public books. This endpoint does not require an Authorization header."
    )
    @GetMapping("/api/library/public/books")
    public List<Book> getPublicBooks() {
        return bookRepository.findPublicBooks();
    }

    @Operation(
            summary = "Get members books",
            description = """
                    Returns books intended for authenticated USER or ADMIN principals.
                    This fake backend does not validate JWTs. It only receives the Authorization header
                    forwarded by the gateway when the request is authorized.
                    """
    )
    @GetMapping("/api/library/members/books")
    public List<Book> getMembersBooks(HttpServletRequest request) {
        readAuthorizationHeader(request);
        return bookRepository.findMembersBooks();
    }

    @Operation(
            summary = "Get admin books",
            description = """
                    Returns books intended for ADMIN principals.
                    This fake backend does not validate JWTs. It only receives the Authorization header
                    forwarded by the gateway when the request is authorized.
                    """
    )
    @GetMapping("/api/library/admin/books")
    public List<Book> getAdminBooks(HttpServletRequest request) {
        readAuthorizationHeader(request);
        return bookRepository.findAdminBooks();
    }

    private String readAuthorizationHeader(HttpServletRequest request) {
        return request.getHeader(HttpHeaders.AUTHORIZATION);
    }
}