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

    private String batter;
    private String bowler;
    private Integer runs;
    private Boolean wicket;

    @ManyToOne
    @JoinColumn(name = "over_id")
    private Over over;
}
