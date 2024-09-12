package com.indium.backend_assignment.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
@Entity
@Table(name = "Matches")
@Data
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer matchId;

    private String city;
    private String venue;
    private LocalDate matchDate;

    @OneToMany(mappedBy = "match")
    private List<Team> teams;
}
