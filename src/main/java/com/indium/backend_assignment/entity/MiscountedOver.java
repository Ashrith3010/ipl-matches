package com.indium.backend_assignment.entity;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "MiscountedOvers")
@Data
public class MiscountedOver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer miscountedOversId;

    @ManyToOne
    @JoinColumn(name = "innings_id")
    private Innings innings;

    private Integer overNumber;
    private Integer miscountedBalls;

    // Getters and setters
}