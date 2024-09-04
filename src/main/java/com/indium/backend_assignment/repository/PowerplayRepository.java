package com.indium.backend_assignment.repository;

import com.indium.backend_assignment.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PowerplayRepository extends JpaRepository<Powerplay, Integer> {
}