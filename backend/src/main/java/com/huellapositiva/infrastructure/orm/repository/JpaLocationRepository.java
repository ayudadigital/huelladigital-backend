package com.huellapositiva.infrastructure.orm.repository;

import com.huellapositiva.infrastructure.orm.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaLocationRepository extends JpaRepository<Location, Integer> {

    @Query("FROM Location l LEFT JOIN FETCH l.proposal WHERE l.id = :id")
    Optional<Location> findByIdWithProposals(@Param("id") Integer id);
}
