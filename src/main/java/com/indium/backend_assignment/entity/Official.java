package com.indium.backend_assignment.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Officials")
@Data
public class Official {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer officialId;

    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;

    private String role;
    private String name;

    // Getters and setters
}