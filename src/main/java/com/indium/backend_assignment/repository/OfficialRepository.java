package com.indium.backend_assignment.repository;

import com.indium.backend_assignment.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfficialRepository extends JpaRepository<Official, Integer> {
    List<Official> findByMatchAndRole(Match match, String role);

}
