package com.indium.backend_assignment.repository;


import com.indium.backend_assignment.entity.Match;
import com.indium.backend_assignment.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Integer> {
    Team findByTeamNameAndMatch(String teamName, Match match);
    Team findByTeamName(String teamName);

}
