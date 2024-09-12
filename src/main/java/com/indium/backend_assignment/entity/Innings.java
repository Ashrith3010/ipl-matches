package com.indium.backend_assignment.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
@Entity
@Table(name = "Innings")
@Data
public class Innings {
    @Id
    @Column(name = "innings_id" )
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer inningsId;

    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @OneToMany(mappedBy = "innings")
    private List<Over> overs;
}
