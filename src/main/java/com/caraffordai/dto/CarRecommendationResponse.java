package com.caraffordai.dto;

import lombok.*;

/**
 * CarRecommendationResponse DTO – A single car recommendation card.
 *
 * This is returned as a list (Top 3) from GET /api/cars/recommend
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarRecommendationResponse {

    private Long carId;
    private String brand;
    private String model;
    private String variant;
    private String fuelType;

    /** On-road car price (INR) */
    private Double price;

    /** Fuel efficiency (km/L) */
    private Double mileage;

    /** Monthly maintenance cost (INR) */
    private Double maintenanceCost;

    /** Calculated monthly EMI for this car */
    private Double monthlyEmi;

    /** Total monthly ownership cost (EMI + maintenance) */
    private Double totalMonthlyCost;

    /** Percentage of user's income this car consumes */
    private Double incomeUsagePercent;

    /** Why this car is a good fit for the user */
    private String whyRecommended;

    /** Rank in the recommendation list (1 = best fit) */
    private Integer rank;
}
