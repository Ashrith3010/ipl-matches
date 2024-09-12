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

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    private Integer totalRuns; // Add this field
}