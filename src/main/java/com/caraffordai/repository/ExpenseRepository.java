package com.caraffordai.repository;

import com.caraffordai.entity.Expense;
import com.caraffordai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ExpenseRepository – Data access for user expense profiles.
 */
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    /** Find expense record by the associated user */
    Optional<Expense> findByUser(User user);

    /** Direct lookup by user ID to avoid joining User entity */
    Optional<Expense> findByUserId(Long userId);
}
