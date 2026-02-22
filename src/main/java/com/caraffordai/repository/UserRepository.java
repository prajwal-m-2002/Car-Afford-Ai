package com.caraffordai.repository;

import com.caraffordai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository – Data access layer for User entity.
 *
 * Interview Tip: Spring Data JPA auto-generates SQL queries at runtime
 * by parsing method names (e.g., findByEmail → SELECT * FROM users WHERE email
 * = ?)
 * No need to write boilerplate JDBC code.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** Check if a user with this email already exists (for duplicate prevention) */
    Optional<User> findByEmail(String email);

    /** Check email existence for validation */
    boolean existsByEmail(String email);
}
