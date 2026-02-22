package com.caraffordai.repository;

import com.caraffordai.entity.LoanOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * LoanOptionRepository – Data access for loan interest rate configurations.
 */
@Repository
public interface LoanOptionRepository extends JpaRepository<LoanOption, Long> {

    /** Get all loan options for a specific tenure */
    List<LoanOption> findByTenureYearsOrderByInterestRateAsc(Integer tenureYears);

    /** Get the best (lowest interest) loan option for a tenure */
    Optional<LoanOption> findFirstByTenureYearsOrderByInterestRateAsc(Integer tenureYears);
}
