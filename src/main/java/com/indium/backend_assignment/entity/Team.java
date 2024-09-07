package com.indium.backend_assignment.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "Teams")
@Data
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer teamId;

    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;

    private String teamName;
    private Boolean isWinner;

    @OneToMany(mappedBy = "team")
    private List<Player> players;

}