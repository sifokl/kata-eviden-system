package com.saifksibi.eviden.books.model;

import java.util.List;

public record Book(
        Long id,
        String title,
        String description,
        BookCategory category,
        List<String> intendedForRoles
) {
}