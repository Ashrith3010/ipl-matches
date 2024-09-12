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
    @Column(name="match_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer matchId;
    @Column
    private String city;
    @Column
    private String venue;
    @Column(name = "match_date" )
    private LocalDate matchDate;

    @OneToMany(mappedBy = "match")
    private List<Team> teams;
}