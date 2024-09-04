package com.indium.backend_assignment.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Deliveries")
@Data
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer deliveryId;

    @ManyToOne
    @JoinColumn(name = "over_id")
    private Over over;

    private Integer ballNumber;
    private String batterName;
    private String bowlerName;
    private String nonStrikerName;
    private Integer runsScored;
    private Integer extras;
    private Integer totalRuns;
    private Boolean wicket;
    private String wicketKind;
    private String playerOut;
    private String fielder;

    // Getters and setters
}