package com.indium.backend_assignment.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
@Entity
@Table(name = "Teams")
@Data
public class Team {
    @Id
    @Column(name="team_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer teamId;
    @Column(name="team_name")
    private String teamName;

    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;

    @OneToMany(mappedBy = "team")
    private List<Player> players;

    public Team(Integer teamId, String teamName) {
        this.teamId = teamId;
        this.teamName = teamName;
    }
    public Team() {
    }

}