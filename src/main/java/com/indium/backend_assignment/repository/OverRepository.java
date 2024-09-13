package com.indium.backend_assignment.repository;

import com.indium.backend_assignment.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OverRepository extends JpaRepository<Over, Integer> {
    List<Over> findByInnings(Innings innings);


}