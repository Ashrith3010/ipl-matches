package com.indium.backend_assignment.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Players")
@Data
public class Player {
    @Id
    @Column(name="player_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer playerId;
    @Column(name="player_name")
    private String playerName;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;
    @Column
    private Integer totalRuns;
}