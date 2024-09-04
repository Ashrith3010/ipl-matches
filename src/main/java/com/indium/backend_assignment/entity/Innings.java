package com.indium.backend_assignment.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "Innings")
@Data
public class Innings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer inningsId;

    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    private Integer totalRuns;
    private Integer totalWickets;
    private Integer totalOvers;

    @OneToMany(mappedBy = "innings")
    private List<Over> overs;

    @OneToMany(mappedBy = "innings")
    private List<Powerplay> powerplays;

    @OneToMany(mappedBy = "innings")
    private List<MiscountedOver> miscountedOvers;

    // Getters and setters
}