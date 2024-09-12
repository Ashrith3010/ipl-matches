package com.indium.backend_assignment.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Deliveries")
@Data
public class Delivery {
    @Id
    @Column(name="delivery_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer deliveryId;
    @Column
    private String batter;
    @Column
    private String bowler;
    @Column
    private Integer runs;
    @Column
    private Boolean wicket;

    @ManyToOne
    @JoinColumn(name = "over_id")
    private Over over;
}