package com.indium.backend_assignment.repository;

import com.indium.backend_assignment.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Integer> {
    Page<Player> findAllByOrderByTotalRunsDesc(Pageable pageable);
    Player findByPlayerName(String playerName);
}