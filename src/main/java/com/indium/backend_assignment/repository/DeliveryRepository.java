package com.indium.backend_assignment.repository;

import com.indium.backend_assignment.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Integer> {
    List<Delivery> findByBatterNameOrBowlerName(String batterName, String bowlerName);
    List<Delivery> findByBatterName(String batterName);
    List<Delivery> findByBowlerNameAndWicketIsTrue(String bowlerName);
    List<Delivery> findByBatterNameAndOver_Innings_Match(String batterName, Match match);
}