package com.indium.backend_assignment.entity;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Powerplays")
@Data
public class Powerplay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer powerplayId;

    @ManyToOne
    @JoinColumn(name = "innings_id")
    private Innings innings;

    private String type;
    private Integer fromOver;
    private Integer toOver;

    // Getters and setters
}