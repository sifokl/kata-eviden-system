package com.saifksibi.eviden.gateway.domain.model;

import java.util.Objects;

    public record Role(String name) {

        private static final String SPRING_ROLE_PREFIX = "ROLE_";

        public Role {
            Objects.requireNonNull(name, "Role name must not be null");

            name = name.trim();

            if (name.isBlank()) {
                throw new IllegalArgumentException("Role name must not be blank");
            }
        }

        public String asSpringAuthority() {
            return name.startsWith(SPRING_ROLE_PREFIX)
                    ? name
                    : SPRING_ROLE_PREFIX + name;
        }

        public boolean matches(String expectedRole) {
            Objects.requireNonNull(expectedRole, "Expected role must not be null");

            String normalizedCurrent = normalize(name);
            String normalizedExpected = normalize(expectedRole);

            return normalizedCurrent.equals(normalizedExpected);
        }

        private static String normalize(String role) {
            String trimmed = role.trim();

            return trimmed.startsWith(SPRING_ROLE_PREFIX)
                    ? trimmed.substring(SPRING_ROLE_PREFIX.length())
                    : trimmed;
        }
}
