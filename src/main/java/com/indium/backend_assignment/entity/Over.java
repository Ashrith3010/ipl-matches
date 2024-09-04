package com.indium.backend_assignment.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "Overs")
@Data
public class Over {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer overId;

    @ManyToOne
    @JoinColumn(name = "innings_id")
    private Innings innings;

    private Integer overNumber;

    @OneToMany(mappedBy = "over")
    private List<Delivery> deliveries;

    // Getters and setters
}