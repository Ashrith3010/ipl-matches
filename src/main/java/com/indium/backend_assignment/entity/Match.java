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
    private String matchType;
    private Integer overs;
    private Integer ballsPerOver;
    private String winnerTeam;
    private String eventName;
    private String eventSeason;
    private Integer matchNumber;
    private String tossWinner;
    private String tossDecision;
    private String playerOfMatch;
    private String teamType;

    @OneToMany(mappedBy = "match")
    private List<Team> teams;

    @OneToMany(mappedBy = "match")
    private List<Innings> innings;

    @OneToMany(mappedBy = "match")
    private List<Official> officials;

    // Getters and setters
}