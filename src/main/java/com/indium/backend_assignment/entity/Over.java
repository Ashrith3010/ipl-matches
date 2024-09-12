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

    private Integer overNumber;

    @ManyToOne
    @JoinColumn(name = "innings_id")
    private Innings innings;

    @OneToMany(mappedBy = "over")
    private List<Delivery> deliveries;
}
