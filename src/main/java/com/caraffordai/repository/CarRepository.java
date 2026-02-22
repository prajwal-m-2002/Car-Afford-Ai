package com.caraffordai.repository;

import com.caraffordai.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CarRepository – Data access for the car catalog.
 *
 * Interview Tip: The recommendation query finds cars within the user's budget
 * and sorts by price ascending (most affordable first) then by maintenance
 * cost.
 * We use JPQL (Java Persistence Query Language) not native SQL,
 * so it remains database-agnostic.
 */
@Repository
public interface CarRepository extends JpaRepository<Car, Long> {

    /**
     * Core recommendation query:
     * Find all cars priced within the user's max affordable price,
     * sorted by price asc (closest to budget = best value) and maintenance cost
     * asc.
     *
     * @param maxPrice User's maximum affordable car price
     * @param limit    Top N results to return
     */
    @Query(value = "SELECT c FROM Car c WHERE c.price <= :maxPrice ORDER BY c.price DESC, c.maintenanceCost ASC")
    List<Car> findAffordableCars(@Param("maxPrice") Double maxPrice);

    /** Find cars by fuel type (for future EV vs Petrol filtering) */
    List<Car> findByFuelTypeIgnoreCase(String fuelType);

    /** Find cars within a price range */
    List<Car> findByPriceBetweenOrderByPriceAsc(Double minPrice, Double maxPrice);
}
