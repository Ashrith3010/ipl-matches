package com.indium.backend_assignment.entity;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Registry")
@Data
public class Registry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer registryId;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;

    private String playerName;
    private String registryValue;

    // Getters and setters
}