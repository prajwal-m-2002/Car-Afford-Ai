package com.caraffordai.repository;

import com.caraffordai.entity.AffordabilityReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * AffordabilityReportRepository – Data access for generated reports.
 */
@Repository
public interface AffordabilityReportRepository extends JpaRepository<AffordabilityReport, Long> {

    /** Get the latest report for a user */
    Optional<AffordabilityReport> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    /** Get all reports for a user (history) */
    List<AffordabilityReport> findByUserIdOrderByCreatedAtDesc(Long userId);
}
