package com.indium.backend_assignment.repository;

import com.indium.backend_assignment.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Integer> {
    List<Player> findByTeam(Team team);
    Page<Player> findAllByOrderByTotalRunsDesc(Pageable pageable);
    Page<Player> findAllByOrderByTotalWicketsDesc(Pageable pageable);

}
