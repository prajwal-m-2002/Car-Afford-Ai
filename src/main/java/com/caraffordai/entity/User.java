package com.caraffordai.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * User Entity – Represents a registered user of CarAfford AI.
 *
 * Interview Tip: @Entity maps this class to the 'users' table.
 * We use Lombok annotations to avoid boilerplate getters/setters.
 *
 * Relationships:
 * - One User → One Expense record (OneToOne)
 * - One User → Many Affordability Reports (OneToMany)
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /** Monthly gross income in INR */
    @Column(name = "monthly_income", nullable = false)
    private Double monthlyIncome;

    /**
     * BCrypt hashed password. Nullable for backward compatibility with existing
     * records.
     * BCrypt output is always 60 chars.
     */
    @Column(name = "password_hash", length = 60)
    private String passwordHash;
}
