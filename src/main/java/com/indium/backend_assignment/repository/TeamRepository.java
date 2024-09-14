package com.indium.backend_assignment.repository;

import com.indium.backend_assignment.entity.Match;
import com.indium.backend_assignment.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Integer> {

    @Query("SELECT t FROM Team t JOIN t.matches m WHERE t.teamName = :teamName AND m = :match")
    Team findByTeamNameAndMatch(@Param("teamName") String teamName, @Param("match") Match match);

    Team findByTeamName(String teamName);
}