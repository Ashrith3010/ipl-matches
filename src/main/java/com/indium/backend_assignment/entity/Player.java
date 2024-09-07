package com.indium.backend_assignment.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Players")
@Data
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer playerId;

    private String playerName;

    private Integer totalRuns; // Ensure this field exists

    private Integer totalWickets; // Add this field

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    // Getters and setters (Lombok will generate these)
}
