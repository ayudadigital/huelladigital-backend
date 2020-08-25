package com.huellapositiva.infrastructure.orm.repository;

import com.huellapositiva.infrastructure.orm.entities.JpaESAL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaESALRepository extends JpaRepository<JpaESAL, Integer> {

    @Query("FROM JpaESAL o WHERE o.name = :name")
    Optional<JpaESAL> findByName(@Param("name") String name);

    @Query("FROM JpaESAL o WHERE o.id = :id")
    Optional<JpaESAL> findByNaturalId(@Param("id") String id);

    @Modifying
    @Query("DELETE FROM JpaESAL e WHERE e.id = :id")
    Integer deleteByNaturalId(@Param("id")String id);
}
