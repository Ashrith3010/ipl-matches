package com.indium.backend_assignment.repository;

import com.indium.backend_assignment.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Integer> {
    List<Match> findByMatchDate(LocalDate date);
    Match findByCityAndVenueAndMatchDate(String city, String venue, LocalDate matchDate);

}