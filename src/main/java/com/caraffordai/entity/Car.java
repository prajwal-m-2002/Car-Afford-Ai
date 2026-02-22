package com.caraffordai.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Car Entity – Represents a car available in the system's catalog.
 *
 * Interview Tip: The recommendation engine queries this table
 * to find cars where: car.price <= user's maxAffordablePrice.
 * We rank by (price asc, maintenance_cost asc) to surface best value.
 *
 * Extension Point: Add ev_range, emission_type for future EV vs Petrol
 * analysis.
 */
@Entity
@Table(name = "cars", indexes = {
        @Index(name = "idx_cars_price", columnList = "price"),
        @Index(name = "idx_cars_fuel_type", columnList = "fuel_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String brand;

    @Column(nullable = false, length = 100)
    private String model;

    /** Trim/Variant name (e.g. VXi, ZXi, L, Titanium) */
    @Column(length = 100)
    private String variant;

    /** On-road price in INR */
    @Column(nullable = false)
    private Double price;

    /** Fuel efficiency in km/L */
    @Column(nullable = false)
    private Double mileage;

    /** Estimated monthly maintenance cost in INR */
    @Column(name = "maintenance_cost", nullable = false)
    private Double maintenanceCost;

    /** e.g., PETROL, DIESEL, ELECTRIC, HYBRID */
    @Column(name = "fuel_type", nullable = false, length = 20)
    private String fuelType;

    /** Body segment: Hatchback, Sedan, SUV, MicroSUV, Electric */
    @Column(name = "segment", length = 50)
    private String segment;

    /** Short description for recommendation UI */
    @Column(length = 500)
    private String description;
}
