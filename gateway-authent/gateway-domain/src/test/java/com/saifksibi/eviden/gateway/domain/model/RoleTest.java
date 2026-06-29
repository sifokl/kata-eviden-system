package com.saifksibi.eviden.gateway.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    @DisplayName("Should create valid role")
    void shouldCreateValidRole() {
        Role role = new Role("ADMIN");
        assertEquals("ADMIN", role.name());
    }

    @Test
    @DisplayName("Should normalize role name")
    void shouldNormalizeRoleName() {
        Role role = new Role("  ADMIN  ");
        assertEquals("ADMIN", role.name());
    }

    @Test
    @DisplayName("Should reject blank role name")
    void shouldRejectBlankRoleName() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Role("   ")
        );

        assertEquals("Role name must not be blank", exception.getMessage());
    }

    @Test
    @DisplayName("Should match role without prefix")
    void shouldMatchRoleWithoutPrefix() {
        Role role = new Role("ADMIN");

        assertTrue(role.matches("ADMIN"));
        assertTrue(role.matches("ROLE_ADMIN"));
        assertFalse(role.matches("USER"));
    }
}